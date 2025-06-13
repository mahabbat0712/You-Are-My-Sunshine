package com.fitnesscenter.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

/**
 * Database configuration for the application.
 * Provides DataSource and JdbcTemplate beans.
 */
@Configuration
@PropertySource("classpath:application.properties")
public class DatabaseConfig {
    
    private static final Logger logger = LogManager.getLogger(DatabaseConfig.class);
    
    @Value("${jdbc.driver:org.postgresql.Driver}")
    private String jdbcDriver;
    
    @Value("${jdbc.url:jdbc:postgresql://localhost:5432/fitness_center}")
    private String jdbcUrl;
    
    @Value("${jdbc.username:postgres}")
    private String jdbcUsername;
    
    @Value("${jdbc.password:postgres}")
    private String jdbcPassword;
    
    @Bean
    public DataSource dataSource() {
        logger.info("Initializing DataSource with URL: {}", jdbcUrl);
        
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(jdbcDriver);
        dataSource.setUrl(jdbcUrl);
        dataSource.setUsername(jdbcUsername);
        dataSource.setPassword(jdbcPassword);
        
        return dataSource;
    }
    
    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        logger.info("Creating JdbcTemplate bean");
        return new JdbcTemplate(dataSource);
    }
} 