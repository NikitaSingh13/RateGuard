package com.example.rateguard.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "rateguard")
public class RateLimitConfig {

    private List<ClientConfig> clients;
    private List<PolicyConfig> policies;

    public List<ClientConfig> getClients() {
        return clients;
    }

    public void setClients(List<ClientConfig> clients) {
        this.clients = clients;
    }

    public List<PolicyConfig> getPolicies() {
        return policies;
    }

    public void setPolicies(List<PolicyConfig> policies) {
        this.policies = policies;
    }

    public static class ClientConfig {
        private String clientId;
        private String apiKey;
        private String policyId;

        public String getClientId() { return clientId; }
        public void setClientId(String clientId) { this.clientId = clientId; }

        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }

        public String getPolicyId() { return policyId; }
        public void setPolicyId(String policyId) { this.policyId = policyId; }
    }

    public static class PolicyConfig {
        private String policyId;
        private long capacity;
        private long refillTokens;
        private long refillPeriodMillis;

        public String getPolicyId() { return policyId; }
        public void setPolicyId(String policyId) { this.policyId = policyId; }

        public long getCapacity() { return capacity; }
        public void setCapacity(long capacity) { this.capacity = capacity; }

        public long getRefillTokens() { return refillTokens; }
        public void setRefillTokens(long refillTokens) {
            this.refillTokens = refillTokens;
        }

        public long getRefillPeriodMillis() { return refillPeriodMillis; }
        public void setRefillPeriodMillis(long refillPeriodMillis) {
            this.refillPeriodMillis = refillPeriodMillis;
        }
    }
}