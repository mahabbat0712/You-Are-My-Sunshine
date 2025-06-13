package com.fitnesscenter.jdbc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Custom thread-safe connection pool implementation.
 * Manages database connections for efficient reuse.
 */
public class ConnectionPool {
    private static final Logger logger = LogManager.getLogger(ConnectionPool.class);
    private static ConnectionPool instance;
    private static final Lock instanceLock = new ReentrantLock();

    private final String url;
    private final Properties properties;
    private final int maxConnections;
    private final BlockingQueue<Connection> availableConnections;
    private final List<Connection> usedConnections;
    private final Lock connectionsLock = new ReentrantLock();

    /**
     * Private constructor to enforce singleton pattern
     * 
     * @param url Database URL
     * @param properties Connection properties (username, password, etc.)
     * @param maxConnections Maximum number of connections in the pool
     */
    private ConnectionPool(String url, Properties properties, int maxConnections) {
        this.url = url;
        this.properties = properties;
        this.maxConnections = maxConnections;
        this.availableConnections = new LinkedBlockingQueue<>(maxConnections);
        this.usedConnections = new ArrayList<>(maxConnections);
        
        // Initialize pool with connections
        initializeConnectionPool();
    }

    /**
     * Get singleton instance of the connection pool
     * 
     * @param url Database URL
     * @param properties Connection properties
     * @param maxConnections Maximum number of connections
     * @return ConnectionPool singleton instance
     */
    public static ConnectionPool getInstance(String url, Properties properties, int maxConnections) {
        if (instance == null) {
            instanceLock.lock();
            try {
                if (instance == null) {
                    instance = new ConnectionPool(url, properties, maxConnections);
                    logger.info("Connection pool initialized with max {} connections", maxConnections);
                }
            } finally {
                instanceLock.unlock();
            }
        }
        return instance;
    }

    /**
     * Initialize the connection pool with connections up to maxConnections
     */
    private void initializeConnectionPool() {
        try {
            for (int i = 0; i < maxConnections; i++) {
                availableConnections.add(createNewConnection());
            }
        } catch (SQLException e) {
            logger.error("Failed to initialize connection pool", e);
            throw new RuntimeException("Failed to initialize connection pool", e);
        }
    }

    /**
     * Create a new database connection
     * 
     * @return A new Connection object
     * @throws SQLException if connection creation fails
     */
    private Connection createNewConnection() throws SQLException {
        try {
            return DriverManager.getConnection(url, properties);
        } catch (SQLException e) {
            logger.error("Failed to create database connection", e);
            throw e;
        }
    }

    /**
     * Get a connection from the pool
     * 
     * @return A database connection
     * @throws SQLException if no connection is available
     */
    public Connection getConnection() throws SQLException {
        connectionsLock.lock();
        try {
            Connection connection = availableConnections.poll();
            
            // If no connection is available, try to create a new one if under maxConnections
            if (connection == null) {
                if (usedConnections.size() < maxConnections) {
                    connection = createNewConnection();
                    logger.debug("Created new connection as pool was empty");
                } else {
                    logger.warn("Connection pool exhausted. Max connections reached: {}", maxConnections);
                    throw new SQLException("Connection pool exhausted");
                }
            }
            
            // Validate connection before providing it
            if (!isConnectionValid(connection)) {
                logger.info("Found invalid connection in pool, creating new one");
                connection = createNewConnection();
            }
            
            usedConnections.add(connection);
            logger.debug("Connection obtained from pool. Available: {}, Used: {}", 
                    availableConnections.size(), usedConnections.size());
            return connection;
        } finally {
            connectionsLock.unlock();
        }
    }

    /**
     * Release a connection back to the pool
     * 
     * @param connection Connection to release
     */
    public void releaseConnection(Connection connection) {
        connectionsLock.lock();
        try {
            if (connection != null) {
                usedConnections.remove(connection);
                if (isConnectionValid(connection)) {
                    availableConnections.add(connection);
                    logger.debug("Connection returned to pool. Available: {}, Used: {}", 
                            availableConnections.size(), usedConnections.size());
                } else {
                    try {
                        connection.close();
                        availableConnections.add(createNewConnection());
                        logger.debug("Invalid connection replaced with new connection");
                    } catch (SQLException e) {
                        logger.error("Failed to close invalid connection or create new one", e);
                    }
                }
            }
        } finally {
            connectionsLock.unlock();
        }
    }

    /**
     * Check if a connection is valid
     * 
     * @param connection Connection to check
     * @return true if connection is valid, false otherwise
     */
    private boolean isConnectionValid(Connection connection) {
        try {
            return connection != null && !connection.isClosed() && connection.isValid(1);
        } catch (SQLException e) {
            logger.warn("Failed to validate connection", e);
            return false;
        }
    }

    /**
     * Close all connections in the pool
     */
    public void shutdown() {
        connectionsLock.lock();
        try {
            // Close all used connections
            for (Connection connection : usedConnections) {
                try {
                    if (!connection.isClosed()) {
                        connection.close();
                    }
                } catch (SQLException e) {
                    logger.error("Failed to close used connection during shutdown", e);
                }
            }
            usedConnections.clear();
            
            // Close all available connections
            Connection connection;
            while ((connection = availableConnections.poll()) != null) {
                try {
                    if (!connection.isClosed()) {
                        connection.close();
                    }
                } catch (SQLException e) {
                    logger.error("Failed to close available connection during shutdown", e);
                }
            }
            
            logger.info("Connection pool shut down");
        } finally {
            connectionsLock.unlock();
        }
    }

    /**
     * Get the current size of the connection pool
     * 
     * @return Total number of connections (used + available)
     */
    public int getPoolSize() {
        connectionsLock.lock();
        try {
            return availableConnections.size() + usedConnections.size();
        } finally {
            connectionsLock.unlock();
        }
    }
} 