package com.example.rateguard.service;

import com.example.rateguard.config.RateLimitConfig;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Optional;

@Service
public class ClientResolver {

    private final RateLimitConfig properties;

    public ClientResolver(RateLimitConfig properties) {
        this.properties = properties;
    }

    public Optional<RateLimitConfig.ClientConfig> resolve(
            String suppliedApiKey) {

        if (suppliedApiKey == null || suppliedApiKey.isBlank()) {
            return Optional.empty();
        }

        return properties.getClients().stream()
                .filter(client -> client.getApiKey() != null)
                .filter(client -> MessageDigest.isEqual(
                        client.getApiKey().getBytes(StandardCharsets.UTF_8),
                        suppliedApiKey.getBytes(StandardCharsets.UTF_8)
                ))
                .findFirst();
    }
}