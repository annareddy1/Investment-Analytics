package com.marketlens.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * ✅ Request Logging Filter
 * - Adds X-Request-Id to every HTTP request (generates UUID if missing)
 * - Propagates requestId to response headers
 * - Logs method, path, status, duration for every request
 * - Adds requestId to MDC for correlation in all log lines
 *
 * Every log line will include: [reqId=abc123 analysisId=def456]
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final String REQUEST_ID_HEADER = "X-Request-Id";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        long startTime = System.currentTimeMillis();

        // Extract or generate request ID
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }

        // Add to MDC for correlation (included in every log line)
        MDC.put(MdcKeys.REQUEST_ID, requestId);

        // Add to response headers (for client-side correlation)
        response.setHeader(REQUEST_ID_HEADER, requestId);

        try {
            // Process the request
            filterChain.doFilter(request, response);

        } finally {
            // Log request completion with method, path, status, duration
            long durationMs = System.currentTimeMillis() - startTime;
            log.info("HTTP {} {} - {} - {}ms",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    durationMs);

            // Clean up MDC (prevent memory leaks in thread pools)
            MDC.clear();
        }
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        // Skip logging for health checks (too noisy)
        String path = request.getRequestURI();
        return path.startsWith("/actuator/health");
    }
}
