package com.example.rateguard.service;

import com.example.rateguard.config.RateLimitConfig;
import com.example.rateguard.model.RateLimitPolicy;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PolicyProvider{

    private final RateLimitConfig properties;

    public PolicyProvider(RateLimitConfig properties) {
        this.properties = properties;
    }

    public Optional<RateLimitPolicy> resolve(String policyId) {
        return properties.getPolicies().stream()
                .filter(policy -> policy.getPolicyId().equals(policyId))
                .findFirst()
                .map(policy -> new RateLimitPolicy(
                        policy.getPolicyId(),
                        policy.getCapacity(),
                        policy.getRefillTokens(),
                        policy.getRefillPeriodMillis() / 1000
                ));
    }
}