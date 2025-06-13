package com.fitnesscenter.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

/**
 * Configuration bean that runs database migrations at application startup
 */
@Configuration
public class DatabaseMigrationRunner {
    private static final Logger logger = LogManager.getLogger(DatabaseMigrationRunner.class);
    
    private final JdbcTemplate jdbcTemplate;
    private final ResourceLoader resourceLoader;
    private final ResourcePatternResolver resourceResolver;
    
    @Autowired
    public DatabaseMigrationRunner(JdbcTemplate jdbcTemplate, ResourceLoader resourceLoader) {
        this.jdbcTemplate = jdbcTemplate;
        this.resourceLoader = resourceLoader;
        this.resourceResolver = new PathMatchingResourcePatternResolver(resourceLoader);
    }
    
    /**
     * Run database migrations on application startup
     */
    @PostConstruct
    public void runMigrations() {
        logger.info("Running database migrations...");
        
        try {
            // Load migration scripts from classpath:db/migration directory
            Resource[] resources = loadResourcesFromClasspath();
            
            if (resources != null && resources.length > 0) {
                executeMigrationScripts(resources);
            } else {
                logger.info("No migration scripts found");
            }
        } catch (IOException e) {
            logger.error("Error running database migrations", e);
        }
    }
    
    private Resource[] loadResourcesFromClasspath() throws IOException {
        return resourceResolver.getResources("classpath:db/migration/*.sql");
    }
    
    private void executeMigrationScripts(Resource[] resources) {
        logger.info("Found {} migration scripts", resources.length);
        
        try {
            for (Resource resource : resources) {
                String scriptName = resource.getFilename();
                String sql = readResourceContent(resource);
                
                logger.info("Executing migration script: {}", scriptName);
                jdbcTemplate.execute(sql);
                logger.info("Successfully executed migration script: {}", scriptName);
            }
        } catch (Exception e) {
            logger.error("Error executing migration scripts", e);
        }
    }
    
    private String readResourceContent(Resource resource) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
} 