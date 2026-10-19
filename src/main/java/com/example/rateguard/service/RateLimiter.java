package com.example.rateguard.service;

import com.example.rateguard.model.RateLimitDecision;
import com.example.rateguard.model.RateLimitPolicy;

/**
 * Given a client's identity and rate-limit policy, evaluate the request and return a decision.
 */
public interface RateLimiter {

    /**
     * Interface method to identify which bucket to check.
     * @param bucketKey
     * @param policy
     * @return
     */
    RateLimitDecision check(String bucketKey, RateLimitPolicy policy);
}
