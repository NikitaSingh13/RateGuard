package com.example.rateguard.service;

import com.example.rateguard.config.RateLimitConfig;
import com.example.rateguard.exception.InvalidApiKeyException;
import com.example.rateguard.model.RateLimitDecision;
import com.example.rateguard.model.RateLimitPolicy;
import com.example.rateguard.repository.RateLimitStore;
import org.springframework.stereotype.Service;

@Service
public class RateLimitService {

    private final ClientResolver clientResolver;
    private final PolicyProvider policyProvider;
    private final RateLimitStore rateLimitStore;

    public RateLimitService(
            ClientResolver clientResolver,
            PolicyProvider policyProvider,
            RateLimitStore rateLimitStore) {
        this.clientResolver = clientResolver;
        this.policyProvider = policyProvider;
        this.rateLimitStore = rateLimitStore;
    }

    public RateLimitDecision check(String apiKey) {
        RateLimitConfig.ClientConfig client = clientResolver
                .resolve(apiKey)
                .orElseThrow(() ->
                        new InvalidApiKeyException("Invalid API key"));

        RateLimitPolicy policy = policyProvider
                .resolve(client.getPolicyId())
                .orElseThrow(() ->
                        new IllegalStateException("Configured policy not found"));

        return rateLimitStore.checkAndConsume(
                client.getClientId(),
                policy.getCapacity(),
                policy.getRefillTokens(),
                policy.getRefillPeriodSeconds() * 1000L
        );
    }
}