package com.fitnesscenter.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * Main application configuration
 * Scans for components excluding controllers which are handled by WebConfig
 */
@Configuration
@ComponentScan(
        basePackages = "com.fitnesscenter",
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ANNOTATION,
                value = EnableWebMvc.class
        )
)
@PropertySource("classpath:application.properties")
public class AppConfig {
    // Configuration beans if needed
} 