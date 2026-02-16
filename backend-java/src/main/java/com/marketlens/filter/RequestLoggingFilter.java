package com.marketlens.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.UUID;

/**
 * ✅ Request/Response Logging Filter
 * - Logs HTTP method, URI, status code, duration
 * - Adds request ID for tracing
 * - Excludes actuator endpoints to reduce noise
 * - Only logs in DEBUG mode or production sampling
 */
@Component
@Slf4j
public class RequestLoggingFilter implements Filter {

    private static final String REQUEST_ID_HEADER = "X-Request-ID";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Skip logging for actuator endpoints
        String uri = httpRequest.getRequestURI();
        if (uri.startsWith("/actuator/")) {
            chain.doFilter(request, response);
            return;
        }

        // Generate or use existing request ID
        String requestId = httpRequest.getHeader(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isEmpty()) {
            requestId = UUID.randomUUID().toString();
        }

        // Add request ID to response headers for client tracing
        httpResponse.setHeader(REQUEST_ID_HEADER, requestId);

        long startTime = System.currentTimeMillis();

        try {
            // Continue filter chain
            chain.doFilter(request, response);

        } finally {
            long duration = System.currentTimeMillis() - startTime;

            // Log request completion (only in DEBUG or if error)
            if (log.isDebugEnabled() || httpResponse.getStatus() >= 400) {
                log.info("REQUEST | method={} | uri={} | status={} | duration={}ms | requestId={}",
                        httpRequest.getMethod(),
                        uri,
                        httpResponse.getStatus(),
                        duration,
                        requestId);
            }

            // Log errors with more detail
            if (httpResponse.getStatus() >= 500) {
                log.error("SERVER ERROR | method={} | uri={} | status={} | duration={}ms | requestId={}",
                        httpRequest.getMethod(),
                        uri,
                        httpResponse.getStatus(),
                        duration,
                        requestId);
            }
        }
    }
}
