package com.marketlens.logging;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;
import org.springframework.lang.NonNull;

import java.util.Map;

/**
 * ✅ MDC Task Decorator for Async Threads
 * - Propagates MDC context from parent thread to async threads
 * - Ensures analysisId, ticker, period appear in async processing logs
 *
 * Without this, MDC context (requestId, analysisId) would be lost when
 * Spring's @Async executor creates a new thread for background jobs.
 */
public class MdcTaskDecorator implements TaskDecorator {

    @Override
    @NonNull
    public Runnable decorate(@NonNull Runnable runnable) {
        // Capture MDC from current thread (the HTTP request thread)
        Map<String, String> contextMap = MDC.getCopyOfContextMap();

        return () -> {
            try {
                // Restore MDC in async thread (the background job thread)
                if (contextMap != null) {
                    MDC.setContextMap(contextMap);
                }
                runnable.run();
            } finally {
                // Clean up MDC after task completes
                MDC.clear();
            }
        };
    }
}
