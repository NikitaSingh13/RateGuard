package com.example.rateguard.filter;

import com.example.rateguard.exception.InvalidApiKeyException;
import com.example.rateguard.model.RateLimitDecision;
import com.example.rateguard.service.RateLimitService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;

    public RateLimitFilter(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String apiKey = request.getHeader("X-API-Key");

        try {
            RateLimitDecision decision = rateLimitService.check(apiKey);

            if (!decision.isAllowed()) {
                response.setStatus(429);
                response.setHeader(
                        "Retry-After",
                        String.valueOf(decision.getRetryAfterSeconds())
                );
                response.setContentType("application/json");
                response.getWriter().write(
                        "{\"error\":\"Rate limit exceeded\"}"
                );
                return;
            }

            filterChain.doFilter(request, response);

        } catch (InvalidApiKeyException exception) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"error\":\"Invalid or missing API key\"}"
            );
        }
    }
}