
package com.example.rateguard.service;

import com.example.rateguard.model.RateLimitDecision;
import com.example.rateguard.model.RateLimitPolicy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

class TokenBucketRateLimiterTest {

    private MutableClock clock;
    private TokenBucketRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(
                Instant.parse("2026-01-01T00:00:00Z")
        );

        rateLimiter = new TokenBucketRateLimiter(clock);
    }

    @Test
    void shouldAllowRequestsUntilCapacityIsExhausted() {
        RateLimitPolicy policy =
                new RateLimitPolicy("test", 2, 1, 1);

        RateLimitDecision first =
                rateLimiter.check("client-A", policy);

        RateLimitDecision second =
                rateLimiter.check("client-A", policy);

        RateLimitDecision third =
                rateLimiter.check("client-A", policy);

        assertTrue(first.isAllowed());
        assertEquals(1, first.getRemainingTokens());

        assertTrue(second.isAllowed());
        assertEquals(0, second.getRemainingTokens());

        assertFalse(third.isAllowed());
        assertEquals(1, third.getRetryAfterSeconds());
    }

    @Test
    void shouldRefillTokensWhenTimePasses() {
        RateLimitPolicy policy =
                new RateLimitPolicy("test", 2, 1, 1);

        rateLimiter.check("client-A", policy);
        rateLimiter.check("client-A", policy);

        RateLimitDecision rejected =
                rateLimiter.check("client-A", policy);

        assertFalse(rejected.isAllowed());

        clock.advance(Duration.ofSeconds(1));

        RateLimitDecision afterRefill =
                rateLimiter.check("client-A", policy);

        assertTrue(afterRefill.isAllowed());
        assertEquals(0, afterRefill.getRemainingTokens());
    }

    @Test
    void shouldKeepBucketsIndependentForDifferentClients() {
        RateLimitPolicy policy =
                new RateLimitPolicy("test", 1, 1, 10);

        RateLimitDecision clientAFirst =
                rateLimiter.check("client-A", policy);

        RateLimitDecision clientASecond =
                rateLimiter.check("client-A", policy);

        RateLimitDecision clientBFirst =
                rateLimiter.check("client-B", policy);

        assertTrue(clientAFirst.isAllowed());
        assertFalse(clientASecond.isAllowed());
        assertTrue(clientBFirst.isAllowed());
    }

    @Test
    void shouldRoundRetryTimeUpToWholeSeconds() {
        RateLimitPolicy policy =
                new RateLimitPolicy("test", 1, 2, 1);

        rateLimiter.check("client-A", policy);

        // At 2 tokens/sec, 250 ms replenishes 0.5 tokens.
        clock.advance(Duration.ofMillis(250));

        RateLimitDecision decision =
                rateLimiter.check("client-A", policy);

        assertFalse(decision.isAllowed());
        assertEquals(1, decision.getRetryAfterSeconds());
    }

    private static class MutableClock extends Clock {

        private Instant currentTime;

        MutableClock(Instant initialTime) {
            this.currentTime = initialTime;
        }

        void advance(Duration duration) {
            currentTime = currentTime.plus(duration);
        }

        @Override
        public Instant instant() {
            return currentTime;
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return Clock.fixed(currentTime, zone);
        }
    }
}
