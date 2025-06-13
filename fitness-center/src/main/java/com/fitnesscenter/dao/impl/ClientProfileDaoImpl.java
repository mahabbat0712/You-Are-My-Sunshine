package com.fitnesscenter.dao.impl;

import com.fitnesscenter.dao.AbstractDao;
import com.fitnesscenter.dao.AccountTypeDao;
import com.fitnesscenter.dao.ClientProfileDao;
import com.fitnesscenter.dao.UserDao;
import com.fitnesscenter.model.AccountType;
import com.fitnesscenter.model.ClientProfile;
import com.fitnesscenter.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of ClientProfileDao interface using JDBC
 */
@Repository
public class ClientProfileDaoImpl extends AbstractDao<ClientProfile, Long> implements ClientProfileDao {
    
    private static final Logger logger = LogManager.getLogger(ClientProfileDaoImpl.class);
    
    private final UserDao userDao;
    private final AccountTypeDao accountTypeDao;
    
    @Autowired
    public ClientProfileDaoImpl(UserDao userDao, AccountTypeDao accountTypeDao) {
        this.userDao = userDao;
        this.accountTypeDao = accountTypeDao;
    }
    
    private static final String FIND_BY_USER_ID_SQL = 
            "SELECT user_id, account_type_id, completed_cycles, date_of_birth, health_notes " +
            "FROM client_profiles WHERE user_id = ?";
    
    private static final String FIND_ALL_SQL = 
            "SELECT user_id, account_type_id, completed_cycles, date_of_birth, health_notes " +
            "FROM client_profiles ORDER BY user_id";
    
    private static final String FIND_ALL_PAGINATED_SQL = 
            "SELECT user_id, account_type_id, completed_cycles, date_of_birth, health_notes " +
            "FROM client_profiles ORDER BY user_id LIMIT ? OFFSET ?";
    
    private static final String FIND_BY_ACCOUNT_TYPE_SQL = 
            "SELECT user_id, account_type_id, completed_cycles, date_of_birth, health_notes " +
            "FROM client_profiles WHERE account_type_id = ? " +
            "ORDER BY user_id";
    
    private static final String FIND_BY_ACCOUNT_TYPE_PAGINATED_SQL = 
            "SELECT user_id, account_type_id, completed_cycles, date_of_birth, health_notes " +
            "FROM client_profiles WHERE account_type_id = ? " +
            "ORDER BY user_id LIMIT ? OFFSET ?";
    
    private static final String INSERT_SQL = 
            "INSERT INTO client_profiles (user_id, account_type_id, completed_cycles, date_of_birth, health_notes) " +
            "VALUES (?, ?, ?, ?, ?)";
    
    private static final String UPDATE_SQL = 
            "UPDATE client_profiles SET " +
            "account_type_id = ?, completed_cycles = ?, date_of_birth = ?, health_notes = ? " +
            "WHERE user_id = ?";
    
    private static final String UPDATE_ACCOUNT_TYPE_SQL = 
            "UPDATE client_profiles SET account_type_id = ? WHERE user_id = ?";
    
    private static final String UPDATE_COMPLETED_CYCLES_SQL = 
            "UPDATE client_profiles SET completed_cycles = ? WHERE user_id = ?";
    
    private static final String INCREMENT_COMPLETED_CYCLES_SQL = 
            "UPDATE client_profiles SET completed_cycles = completed_cycles + 1 WHERE user_id = ? " +
            "RETURNING completed_cycles";
    
    private static final String DELETE_SQL = 
            "DELETE FROM client_profiles WHERE user_id = ?";
    
    private static final String COUNT_SQL = 
            "SELECT COUNT(*) FROM client_profiles";
    
    private static final String COUNT_BY_ACCOUNT_TYPE_SQL = 
            "SELECT COUNT(*) FROM client_profiles WHERE account_type_id = ?";
    
    @Override
    public Optional<ClientProfile> findByUserId(Long userId) {
        logger.debug("Finding client profile by user ID: {}", userId);
        return findOne(FIND_BY_USER_ID_SQL, userId);
    }
    
    @Override
    public Optional<ClientProfile> findById(Long userId) {
        logger.debug("Finding client profile by ID (same as user ID): {}", userId);
        return findByUserId(userId);
    }
    
    @Override
    public List<ClientProfile> findAll() {
        logger.debug("Finding all client profiles");
        return findMany(FIND_ALL_SQL);
    }
    
    @Override
    public List<ClientProfile> findAll(int offset, int limit) {
        logger.debug("Finding client profiles with pagination: offset={}, limit={}", offset, limit);
        return findMany(FIND_ALL_PAGINATED_SQL, limit, offset);
    }
    
    @Override
    public List<ClientProfile> findByAccountType(Integer accountTypeId) {
        logger.debug("Finding client profiles by account type ID: {}", accountTypeId);
        return findMany(FIND_BY_ACCOUNT_TYPE_SQL, accountTypeId);
    }
    
    @Override
    public List<ClientProfile> findByAccountType(Integer accountTypeId, int offset, int limit) {
        logger.debug("Finding client profiles by account type ID: {} with pagination: offset={}, limit={}", 
                accountTypeId, offset, limit);
        return findMany(FIND_BY_ACCOUNT_TYPE_PAGINATED_SQL, accountTypeId, limit, offset);
    }
    
    @Override
    public ClientProfile save(ClientProfile clientProfile) {
        if (clientProfile.getUser() == null || clientProfile.getUser().getId() == null) {
            throw new IllegalArgumentException("Client profile must have a user with ID");
        }
        
        Long userId = clientProfile.getUser().getId();
        
        // Check if profile exists
        Optional<ClientProfile> existingProfile = findByUserId(userId);
        if (existingProfile.isPresent()) {
            // Update
            return update(clientProfile);
        } else {
            // Insert new
            return insert(clientProfile);
        }
    }
    
    private ClientProfile insert(ClientProfile clientProfile) {
        logger.debug("Inserting new client profile: {}", clientProfile);
        
        Integer accountTypeId = clientProfile.getAccountType() != null ? 
                clientProfile.getAccountType().getId() : null;
        
        int rowsAffected = update(INSERT_SQL, 
                clientProfile.getUser().getId(),
                accountTypeId,
                clientProfile.getCompletedCycles(),
                clientProfile.getDateOfBirth() != null ? Date.valueOf(clientProfile.getDateOfBirth()) : null,
                clientProfile.getHealthNotes());
        
        if (rowsAffected > 0) {
            return clientProfile;
        } else {
            throw new RuntimeException("Failed to insert client profile for user ID: " + 
                    clientProfile.getUser().getId());
        }
    }
    
    private ClientProfile update(ClientProfile clientProfile) {
        logger.debug("Updating client profile: {}", clientProfile);
        
        Integer accountTypeId = clientProfile.getAccountType() != null ? 
                clientProfile.getAccountType().getId() : null;
        
        int rowsAffected = update(UPDATE_SQL, 
                accountTypeId,
                clientProfile.getCompletedCycles(),
                clientProfile.getDateOfBirth() != null ? Date.valueOf(clientProfile.getDateOfBirth()) : null,
                clientProfile.getHealthNotes(),
                clientProfile.getUser().getId());
        
        if (rowsAffected > 0) {
            return clientProfile;
        } else {
            throw new RuntimeException("Failed to update client profile for user ID: " + 
                    clientProfile.getUser().getId());
        }
    }
    
    @Override
    public boolean updateAccountType(Long userId, Integer accountTypeId) {
        logger.debug("Updating account type for user ID: {} to account type ID: {}", userId, accountTypeId);
        int rowsAffected = update(UPDATE_ACCOUNT_TYPE_SQL, accountTypeId, userId);
        return rowsAffected > 0;
    }
    
    @Override
    public boolean updateCompletedCycles(Long userId, Integer completedCycles) {
        logger.debug("Updating completed cycles for user ID: {} to: {}", userId, completedCycles);
        int rowsAffected = update(UPDATE_COMPLETED_CYCLES_SQL, completedCycles, userId);
        return rowsAffected > 0;
    }
    
    @Override
    public Integer incrementCompletedCycles(Long userId) {
        logger.debug("Incrementing completed cycles for user ID: {}", userId);
        
        Connection connection = null;
        try {
            connection = databaseManager.getConnection();
            PreparedStatement statement = prepareStatement(connection, INCREMENT_COMPLETED_CYCLES_SQL, userId);
            ResultSet resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                int newValue = resultSet.getInt(1);
                logger.debug("Incremented completed cycles for user ID: {} to: {}", userId, newValue);
                return newValue;
            }
            
            logger.warn("Failed to increment completed cycles for user ID: {}", userId);
            return null;
        } catch (SQLException e) {
            logger.error("Error executing incrementCompletedCycles query", e);
            throw new RuntimeException("Error incrementing completed cycles", e);
        } finally {
            if (connection != null) {
                databaseManager.releaseConnection(connection);
            }
        }
    }
    
    @Override
    public boolean deleteById(Long userId) {
        logger.debug("Deleting client profile by user ID: {}", userId);
        int rowsAffected = update(DELETE_SQL, userId);
        return rowsAffected > 0;
    }
    
    @Override
    public long count() {
        logger.debug("Counting all client profiles");
        return count(COUNT_SQL);
    }
    
    @Override
    public long countByAccountType(Integer accountTypeId) {
        logger.debug("Counting client profiles by account type ID: {}", accountTypeId);
        return count(COUNT_BY_ACCOUNT_TYPE_SQL, accountTypeId);
    }
    
    @Override
    protected ClientProfile mapRow(ResultSet resultSet) throws SQLException {
        Long userId = resultSet.getLong("user_id");
        Optional<User> userOpt = userDao.findById(userId);
        
        if (!userOpt.isPresent()) {
            logger.warn("User not found for ID: {}", userId);
            return null;
        }
        
        Integer accountTypeId = resultSet.getObject("account_type_id", Integer.class);
        Optional<AccountType> accountTypeOpt = accountTypeId != null ? 
                accountTypeDao.findById(accountTypeId) : Optional.empty();
        
        ClientProfile clientProfile = new ClientProfile();
        clientProfile.setUser(userOpt.get());
        accountTypeOpt.ifPresent(clientProfile::setAccountType);
        clientProfile.setCompletedCycles(resultSet.getInt("completed_cycles"));
        
        Date dateOfBirth = resultSet.getDate("date_of_birth");
        if (dateOfBirth != null) {
            clientProfile.setDateOfBirth(dateOfBirth.toLocalDate());
        }
        
        clientProfile.setHealthNotes(resultSet.getString("health_notes"));
        
        return clientProfile;
    }
} 