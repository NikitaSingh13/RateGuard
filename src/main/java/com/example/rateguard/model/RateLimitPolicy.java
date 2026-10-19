package com.example.rateguard.model;

/**
 * Defines the rules: maximum bucket capacity and how quickly tokens refill.
 */
public class RateLimitPolicy {

    /**
     * standard-client- Identifies the policy
     */
    private final String policyId;

    /**
     * N : the maximum number of tokens the bucket can hold.
     */
    private final long capacity;

    /**
     * R : how quickly tokens become available again
     */
    private final long refillTokens;

    /**
     * T : the time period in seconds over which the tokens refill
     */
    private final long refillPeriodSeconds;

    /**
     *
     * @param policyId
     * @param capacity
     * @param refillTokens
     * @param refillPeriodSeconds
     */
    public RateLimitPolicy(String policyId, long capacity, long refillTokens, long refillPeriodSeconds) {
        this.policyId = policyId;
        this.capacity = capacity;
        this.refillTokens = refillTokens;
        this.refillPeriodSeconds = refillPeriodSeconds;
    }

    /**
     *
     * @return policyId
     */
    public String getPolicyId() {
        return policyId;
    }

    /**
     *
     * @return capacity
     */
    public long getCapacity() {
        return capacity;
    }

    /**
     *
     * @return refilltokens
     */
    public long getRefillTokens() {
        return refillTokens;
    }

    /**
     * @return refillperiod
     */
    public long getRefillPeriodSeconds() {
        return refillPeriodSeconds;
    }

}
