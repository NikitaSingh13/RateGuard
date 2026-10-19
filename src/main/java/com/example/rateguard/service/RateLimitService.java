package com.example.rateguard.service;

import com.example.rateguard.config.RateLimitConfig;
import com.example.rateguard.exception.InvalidApiKeyException;
import com.example.rateguard.model.RateLimitDecision;
import com.example.rateguard.model.RateLimitPolicy;
import org.springframework.stereotype.Service;

@Service
public class RateLimitService {

    private final ClientResolver clientResolver;
    private final PolicyProvider policyProvider;
    private final RateLimiter rateLimiter;

    public RateLimitService(
            ClientResolver clientResolver,
            PolicyProvider policyProvider,
            RateLimiter rateLimiter) {
        this.clientResolver = clientResolver;
        this.policyProvider = policyProvider;
        this.rateLimiter = rateLimiter;
    }

    public RateLimitDecision check(String apiKey) {
        RateLimitConfig.ClientConfig client = clientResolver
                .resolve(apiKey)
                .orElseThrow(() ->
                        new InvalidApiKeyException("Invalid API key"));

        RateLimitPolicy policy = policyProvider
                .resolve(client.getPolicyId())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Configured policy not found"
                        ));

        return rateLimiter.check(client.getClientId(), policy);
    }
}