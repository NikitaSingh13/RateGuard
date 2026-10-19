package com.example.rateguard.repository;

import com.example.rateguard.model.RateLimitDecision;
import com.example.rateguard.repository.RateLimitStore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RedisRateLimitStoreTest {

    @Autowired
    private RateLimitStore rateLimitStore;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    void shouldAllowOneRequestAndRejectTheNext() {
        String bucketKey = "test:rateguard:" + UUID.randomUUID();

        try {
            RateLimitDecision first = rateLimitStore.checkAndConsume(
                    bucketKey, 1, 1, 60_000
            );

            RateLimitDecision second = rateLimitStore.checkAndConsume(
                    bucketKey, 1, 1, 60_000
            );

            assertTrue(first.isAllowed());
            assertEquals(0, first.getRemainingTokens());

            assertFalse(second.isAllowed());
            assertEquals(0, second.getRemainingTokens());
            assertTrue(second.getRetryAfterSeconds() > 0);
        } finally {
            redisTemplate.delete(bucketKey);
        }
    }

    @Test
    void concurrentRequestsMustNotConsumeTheSameToken() throws Exception {
        String bucketKey = "test:rateguard:concurrent:" + UUID.randomUUID();

        int requestCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(requestCount);
        CountDownLatch ready = new CountDownLatch(requestCount);
        CountDownLatch start = new CountDownLatch(1);

        try {
            List<Future<RateLimitDecision>> futures = new ArrayList<>();

            for (int i = 0; i < requestCount; i++) {
                futures.add(executor.submit(() -> {
                    ready.countDown();

                    if (!start.await(5, TimeUnit.SECONDS)) {
                        throw new IllegalStateException("Start signal timed out");
                    }

                    return rateLimitStore.checkAndConsume(
                            bucketKey, 1, 1, 60_000
                    );
                }));
            }

            assertTrue(ready.await(10, TimeUnit.SECONDS));
            start.countDown();

            AtomicInteger allowedCount = new AtomicInteger();

            for (Future<RateLimitDecision> future : futures) {
                RateLimitDecision decision = future.get(15, TimeUnit.SECONDS);

                if (decision.isAllowed()) {
                    allowedCount.incrementAndGet();
                }
            }

            assertEquals(
                    1,
                    allowedCount.get(),
                    "Exactly one request should consume the single available token"
            );
        } finally {
            start.countDown();
            executor.shutdownNow();
            redisTemplate.delete(bucketKey);
        }
    }
}