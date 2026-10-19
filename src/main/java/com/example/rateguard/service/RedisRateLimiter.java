package com.example.rateguard.service;

import com.example.rateguard.model.RateLimitDecision;
import com.example.rateguard.model.RateLimitPolicy;
import com.example.rateguard.repository.RateLimitStore;
import org.springframework.stereotype.Service;

@Service
public class RedisRateLimiter implements RateLimiter {

    private final RateLimitStore rateLimitStore;

    public RedisRateLimiter(RateLimitStore rateLimitStore) {
        this.rateLimitStore = rateLimitStore;
    }

    @Override
    public RateLimitDecision check(
            String bucketKey,
            RateLimitPolicy policy) {

        return rateLimitStore.checkAndConsume(
                bucketKey,
                policy.getCapacity(),
                policy.getRefillTokens(),
                policy.getRefillPeriodSeconds() * 1000L
        );
    }
}