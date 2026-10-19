package com.example.rateguard.repository;

import com.example.rateguard.model.RateLimitDecision;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RedisRateLimitStore implements RateLimitStore {

    private final StringRedisTemplate redisTemplate;
    private final DefaultRedisScript<List> tokenBucketScript;

    public RedisRateLimitStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;

        this.tokenBucketScript = new DefaultRedisScript<>();
        this.tokenBucketScript.setLocation(
                new ClassPathResource("scripts/token_bucket.lua")
        );
        this.tokenBucketScript.setResultType(List.class);
    }

    @Override
    public RateLimitDecision checkAndConsume(
            String bucketKey,
            long capacity,
            long refillTokens,
            long refillPeriodMillis) {

        List<?> result = redisTemplate.execute(
                tokenBucketScript,
                List.of(bucketKey),
                String.valueOf(capacity),
                String.valueOf(refillTokens),
                String.valueOf(refillPeriodMillis)
        );

        if (result == null || result.size() != 3) {
            throw new IllegalStateException(
                    "Unexpected result from Redis rate limiter"
            );
        }

        boolean allowed = ((Number) result.get(0)).longValue() == 1;
        long remainingTokens = ((Number) result.get(1)).longValue();
        long retryAfterSeconds = ((Number) result.get(2)).longValue();

        return new RateLimitDecision(
                allowed,
                remainingTokens,
                retryAfterSeconds
        );
    }
}