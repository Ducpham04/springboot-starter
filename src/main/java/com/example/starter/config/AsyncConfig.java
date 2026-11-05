package com.example.starter.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/** Enables {@code @Async} (used e.g. by SmtpMailService). Boot provides a virtual-thread friendly executor. */
@Configuration
@EnableAsync
public class AsyncConfig {
}
