
package com.example.rateguard.repository;

import com.example.rateguard.model.RateLimitDecision;

public interface RateLimitStore {

    RateLimitDecision checkAndConsume(
            String bucketKey,
            long capacity,
            long refillTokens,
            long refillPeriodMillis
    );
}
