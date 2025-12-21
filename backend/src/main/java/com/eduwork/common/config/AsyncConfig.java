package com.eduwork.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Enables asynchronous method execution.
 * Methods annotated with @Async will run in separate thread pool.
 */
@Configuration
@EnableAsync
public class AsyncConfig {
    // Using default Spring async configuration
    // Default: SimpleAsyncTaskExecutor
    // For production: customize ThreadPoolTaskExecutor
}
