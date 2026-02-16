package com.marketlens.config;

import com.marketlens.logging.MdcTaskDecorator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * ✅ Async Execution Configuration
 * - Configures thread pool for @Async analysis processing
 * - Proper sizing and rejection policy
 * - Thread naming for debugging
 * - MDC propagation for structured logging in async threads
 */
@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig {

    @Bean(name = "analysisExecutor")
    public Executor analysisExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Core pool size: number of threads to keep alive
        executor.setCorePoolSize(5);

        // Max pool size: maximum threads to create under load
        executor.setMaxPoolSize(10);

        // Queue capacity: how many tasks to queue before rejecting
        executor.setQueueCapacity(25);

        // Thread naming pattern for debugging
        executor.setThreadNamePrefix("analysis-");

        // Rejection policy: what to do when queue is full
        // CallerRunsPolicy: run task in caller's thread (provides back-pressure)
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // Allow core threads to timeout when idle
        executor.setAllowCoreThreadTimeOut(true);

        // Keep alive time for idle threads (60 seconds)
        executor.setKeepAliveSeconds(60);

        // Wait for tasks to complete on shutdown
        executor.setWaitForTasksToCompleteOnShutdown(true);

        // Max wait time on shutdown (30 seconds)
        executor.setAwaitTerminationSeconds(30);

        // ✅ Propagate MDC (requestId, analysisId, ticker, period) to async threads
        executor.setTaskDecorator(new MdcTaskDecorator());

        executor.initialize();

        log.info("Initialized analysis executor with MDC propagation: corePoolSize={}, maxPoolSize={}, queueCapacity={}",
                executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());

        return executor;
    }
}
