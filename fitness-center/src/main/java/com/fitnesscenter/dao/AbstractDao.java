package com.fitnesscenter.dao;

import com.fitnesscenter.jdbc.DatabaseManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Abstract base DAO implementation with common functionality
 *
 * @param <T> Entity type
 * @param <ID> ID type
 */
public abstract class AbstractDao<T, ID> implements BaseDao<T, ID> {
    private static final Logger logger = LogManager.getLogger(AbstractDao.class);
    
    @Autowired
    protected DatabaseManager databaseManager;
    
    /**
     * Execute a query that returns a single result
     *
     * @param sql SQL query
     * @param params Query parameters
     * @return Optional with result entity if found, empty optional otherwise
     */
    protected Optional<T> findOne(String sql, Object... params) {
        Connection connection = null;
        try {
            connection = databaseManager.getConnection();
            PreparedStatement statement = prepareStatement(connection, sql, params);
            ResultSet resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                return Optional.ofNullable(mapRow(resultSet));
            }
            
            return Optional.empty();
        } catch (SQLException e) {
            logger.error("Error executing findOne query: {}", sql, e);
            throw new RuntimeException("Error executing query", e);
        } finally {
            if (connection != null) {
                databaseManager.releaseConnection(connection);
            }
        }
    }
    
    /**
     * Execute a query that returns multiple results
     *
     * @param sql SQL query
     * @param params Query parameters
     * @return List of result entities
     */
    protected List<T> findMany(String sql, Object... params) {
        Connection connection = null;
        try {
            connection = databaseManager.getConnection();
            PreparedStatement statement = prepareStatement(connection, sql, params);
            ResultSet resultSet = statement.executeQuery();
            
            List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                T entity = mapRow(resultSet);
                if (entity != null) {
                    results.add(entity);
                }
            }
            
            return results;
        } catch (SQLException e) {
            logger.error("Error executing findMany query: {}", sql, e);
            throw new RuntimeException("Error executing query", e);
        } finally {
            if (connection != null) {
                databaseManager.releaseConnection(connection);
            }
        }
    }
    
    /**
     * Execute an update query (insert, update, delete)
     *
     * @param sql SQL query
     * @param params Query parameters
     * @return Number of affected rows
     */
    protected int update(String sql, Object... params) {
        Connection connection = null;
        try {
            connection = databaseManager.getConnection();
            PreparedStatement statement = prepareStatement(connection, sql, params);
            return statement.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error executing update query: {}", sql, e);
            throw new RuntimeException("Error executing update", e);
        } finally {
            if (connection != null) {
                databaseManager.releaseConnection(connection);
            }
        }
    }
    
    /**
     * Execute an update query and return generated keys
     *
     * @param sql SQL query
     * @param params Query parameters
     * @return Generated key (ID) if available
     */
    protected Optional<Long> updateAndGetGeneratedKey(String sql, Object... params) {
        Connection connection = null;
        try {
            connection = databaseManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            setParameters(statement, params);
            
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                return Optional.empty();
            }
            
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return Optional.of(generatedKeys.getLong(1));
                } else {
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            logger.error("Error executing update query with generated keys: {}", sql, e);
            throw new RuntimeException("Error executing update", e);
        } finally {
            if (connection != null) {
                databaseManager.releaseConnection(connection);
            }
        }
    }
    
    /**
     * Execute a query to count rows
     *
     * @param sql SQL query
     * @param params Query parameters
     * @return Count result
     */
    protected long count(String sql, Object... params) {
        Connection connection = null;
        try {
            connection = databaseManager.getConnection();
            PreparedStatement statement = prepareStatement(connection, sql, params);
            ResultSet resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                return resultSet.getLong(1);
            }
            
            return 0;
        } catch (SQLException e) {
            logger.error("Error executing count query: {}", sql, e);
            throw new RuntimeException("Error executing count query", e);
        } finally {
            if (connection != null) {
                databaseManager.releaseConnection(connection);
            }
        }
    }
    
    /**
     * Prepare a statement with parameters
     *
     * @param connection Database connection
     * @param sql SQL query
     * @param params Query parameters
     * @return Prepared statement
     * @throws SQLException if an error occurs
     */
    protected PreparedStatement prepareStatement(Connection connection, String sql, Object... params) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sql);
        setParameters(statement, params);
        return statement;
    }
    
    /**
     * Set parameters on a prepared statement
     *
     * @param statement Prepared statement
     * @param params Parameters to set
     * @throws SQLException if an error occurs
     */
    protected void setParameters(PreparedStatement statement, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            statement.setObject(i + 1, params[i]);
        }
    }
    
    /**
     * Start a transaction
     *
     * @param connection Database connection
     * @throws SQLException if an error occurs
     */
    protected void beginTransaction(Connection connection) throws SQLException {
        connection.setAutoCommit(false);
    }
    
    /**
     * Commit a transaction
     *
     * @param connection Database connection
     * @throws SQLException if an error occurs
     */
    protected void commitTransaction(Connection connection) throws SQLException {
        connection.commit();
        connection.setAutoCommit(true);
    }
    
    /**
     * Rollback a transaction
     *
     * @param connection Database connection
     */
    protected void rollbackTransaction(Connection connection) {
        try {
            if (connection != null && !connection.getAutoCommit()) {
                connection.rollback();
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            logger.error("Error rolling back transaction", e);
        }
    }
    
    /**
     * Map a result set row to an entity
     *
     * @param resultSet Result set with current row to map
     * @return Entity instance
     * @throws SQLException if an error occurs
     */
    protected abstract T mapRow(ResultSet resultSet) throws SQLException;
} 