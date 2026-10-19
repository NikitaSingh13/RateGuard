package com.example.rateguard.model;

/**
 * Reports whether the request was allowed,
 * how many tokens remain,
 * and how long the client should wait if rejected.
 */
public class RateLimitDecision {

    /**
     * Whether this request may proceed
     */
    private final boolean allowed;

    /**
     * Available tokens immediately after the decision
     */
    private final long remainingTokens;

    /**
     * Approximate whole seconds until another request can be admitted
     */
    private final long retryAfterSeconds;

    /**
     *
     * @param allowed
     * @param remainingTokens
     * @param retryAfterSeconds
     */
    public RateLimitDecision(boolean allowed, long remainingTokens, long retryAfterSeconds) {
        this.allowed = allowed;
        this.remainingTokens = remainingTokens;
        this.retryAfterSeconds = retryAfterSeconds;
    }

    /**
     *
     * @return
     */
    public boolean isAllowed() {
        return allowed;
    }

    /**
     *
     * @return
     */
    public long getRemainingTokens() {
        return remainingTokens;
    }

    /**
     *
     * @return
     */
    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
