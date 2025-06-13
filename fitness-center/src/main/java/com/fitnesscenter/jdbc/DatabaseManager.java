package com.fitnesscenter.jdbc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Database manager responsible for initializing and managing the connection pool.
 * Provides methods for accessing database connections.
 */
@Component
public class DatabaseManager {
    private static final Logger logger = LogManager.getLogger(DatabaseManager.class);
    
    @Value("${jdbc.url}")
    private String dbUrl;
    
    @Value("${jdbc.username}")
    private String dbUsername;
    
    @Value("${jdbc.password}")
    private String dbPassword;
    
    @Value("${jdbc.pool.size:10}")
    private int poolSize;
    
    private ConnectionPool connectionPool;
    
    /**
     * Initialize the connection pool after Spring creates this bean
     */
    @PostConstruct
    public void init() {
        logger.info("Initializing database manager with connection pool");
        
        try {
            // Load the JDBC driver
            Class.forName("org.postgresql.Driver");
            
            // Create connection properties
            Properties properties = new Properties();
            properties.setProperty("user", dbUsername);
            properties.setProperty("password", dbPassword);
            properties.setProperty("characterEncoding", "UTF-8");
            
            // Initialize connection pool
            connectionPool = ConnectionPool.getInstance(dbUrl, properties, poolSize);
            logger.info("Database manager initialized successfully");
        } catch (ClassNotFoundException e) {
            logger.error("Failed to load database driver", e);
            throw new RuntimeException("Failed to load database driver", e);
        } catch (Exception e) {
            logger.error("Failed to initialize database manager", e);
            throw new RuntimeException("Failed to initialize database manager", e);
        }
    }
    
    /**
     * Get a connection from the pool
     *
     * @return A database connection
     * @throws SQLException if no connection is available
     */
    public Connection getConnection() throws SQLException {
        if (connectionPool == null) {
            logger.error("Connection pool not initialized");
            throw new SQLException("Connection pool not initialized");
        }
        return connectionPool.getConnection();
    }
    
    /**
     * Release a connection back to the pool
     *
     * @param connection Connection to release
     */
    public void releaseConnection(Connection connection) {
        if (connectionPool != null) {
            connectionPool.releaseConnection(connection);
        }
    }
    
    /**
     * Shutdown the connection pool before Spring destroys this bean
     */
    @PreDestroy
    public void shutdown() {
        logger.info("Shutting down database manager");
        if (connectionPool != null) {
            connectionPool.shutdown();
        }
    }
} 