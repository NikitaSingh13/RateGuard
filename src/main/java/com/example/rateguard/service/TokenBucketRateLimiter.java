package com.example.rateguard.service;

import com.example.rateguard.model.RateLimitDecision;
import com.example.rateguard.model.RateLimitPolicy;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * token bucket algorithm to implement the rate limiting middleware
 */
public class TokenBucketRateLimiter implements RateLimiter {

    private final Clock clock;

    //Each bucket key identifies independent state.
    private final Map<String, BucketState> buckets = new HashMap<>();

    public TokenBucketRateLimiter(Clock clock) {
        this.clock = clock;
    }

    /**
     * Why synchronized? -
     * It prevents two threads in this single JVM from modifying the map and consuming the same token concurrently.
     * It does not coordinate separate application instances.
     */
    @Override
    public synchronized RateLimitDecision check(
            String bucketKey,
            RateLimitPolicy policy) {

        Instant now = clock.instant();

        //if new bucket then initialize it with capacity tokens
        BucketState bucket = buckets.computeIfAbsent(
                bucketKey,
                key -> new BucketState(
                        policy.getCapacity(),
                        now
                )
        );

        // Keep existing state within the current policy's capacity.
        bucket.tokens = Math.min(
                bucket.tokens,
                policy.getCapacity()
        );

        // Calculate tokens replenished since the last check.
        if (now.isAfter(bucket.lastRefillTime)) {
            Duration elapsed = Duration.between(
                    bucket.lastRefillTime,
                    now
            );

            double elapsedSeconds =
                    elapsed.getSeconds()
                            + elapsed.getNano() / 1_000_000_000.0;

            double refillRate =
                    (double) policy.getRefillTokens()
                            / policy.getRefillPeriodSeconds();

            bucket.tokens = Math.min(
                    policy.getCapacity(),
                    bucket.tokens + elapsedSeconds * refillRate
            );

            bucket.lastRefillTime = now;
        }

        // One request consumes one token.
        if (bucket.tokens >= 1.0) {
            bucket.tokens -= 1.0;

            return new RateLimitDecision(
                    true,
                    (long) Math.floor(bucket.tokens),
                    0
            );
        }

        // Calculate the time until one token becomes available.
        double refillRate =
                (double) policy.getRefillTokens()
                        / policy.getRefillPeriodSeconds();

        long retryAfterSeconds = (long) Math.ceil(
                (1.0 - bucket.tokens) / refillRate
        );

        return new RateLimitDecision(
                false,
                (long) Math.floor(bucket.tokens),
                Math.max(1, retryAfterSeconds)
        );
    }

    private static class BucketState {

        private double tokens;
        private Instant lastRefillTime;

        private BucketState(
                long capacity,
                Instant now) {

            this.tokens = capacity;
            this.lastRefillTime = now;
        }
    }
}
