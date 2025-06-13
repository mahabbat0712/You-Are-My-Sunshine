package com.fitnesscenter.dao.impl;

import com.fitnesscenter.dao.AbstractDao;
import com.fitnesscenter.dao.AccountTypeDao;
import com.fitnesscenter.model.AccountType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of AccountTypeDao interface using JDBC
 */
@Repository
public class AccountTypeDaoImpl extends AbstractDao<AccountType, Integer> implements AccountTypeDao {
    
    private static final Logger logger = LogManager.getLogger(AccountTypeDaoImpl.class);
    
    private static final String FIND_BY_ID_SQL = 
            "SELECT id, name, description FROM account_types WHERE id = ?";
    
    private static final String FIND_BY_NAME_SQL = 
            "SELECT id, name, description FROM account_types WHERE name = ?";
    
    private static final String FIND_ALL_SQL = 
            "SELECT id, name, description FROM account_types ORDER BY id";
    
    private static final String FIND_ALL_PAGINATED_SQL = 
            "SELECT id, name, description FROM account_types ORDER BY id LIMIT ? OFFSET ?";
    
    private static final String INSERT_SQL = 
            "INSERT INTO account_types (name, description) VALUES (?, ?)";
    
    private static final String UPDATE_SQL = 
            "UPDATE account_types SET name = ?, description = ? WHERE id = ?";
    
    private static final String DELETE_SQL = 
            "DELETE FROM account_types WHERE id = ?";
    
    private static final String COUNT_SQL = 
            "SELECT COUNT(*) FROM account_types";
    
    private static final String EXISTS_BY_NAME_SQL = 
            "SELECT COUNT(*) FROM account_types WHERE name = ?";
    
    @Override
    public Optional<AccountType> findById(Integer id) {
        logger.debug("Finding account type by ID: {}", id);
        return findOne(FIND_BY_ID_SQL, id);
    }
    
    @Override
    public Optional<AccountType> findByName(String name) {
        logger.debug("Finding account type by name: {}", name);
        return findOne(FIND_BY_NAME_SQL, name);
    }
    
    @Override
    public List<AccountType> findAll() {
        logger.debug("Finding all account types");
        return findMany(FIND_ALL_SQL);
    }
    
    @Override
    public List<AccountType> findAll(int offset, int limit) {
        logger.debug("Finding account types with pagination: offset={}, limit={}", offset, limit);
        return findMany(FIND_ALL_PAGINATED_SQL, limit, offset);
    }
    
    @Override
    public AccountType save(AccountType accountType) {
        if (accountType.getId() == null) {
            return insert(accountType);
        } else {
            return update(accountType);
        }
    }
    
    private AccountType insert(AccountType accountType) {
        logger.debug("Inserting new account type: {}", accountType);
        
        Optional<Long> generatedId = updateAndGetGeneratedKey(INSERT_SQL, 
                accountType.getName(), accountType.getDescription());
        
        if (generatedId.isPresent()) {
            accountType.setId(generatedId.get().intValue());
            return accountType;
        } else {
            throw new RuntimeException("Failed to insert account type: " + accountType);
        }
    }
    
    private AccountType update(AccountType accountType) {
        logger.debug("Updating account type: {}", accountType);
        
        int rowsAffected = update(UPDATE_SQL, 
                accountType.getName(), accountType.getDescription(), accountType.getId());
        
        if (rowsAffected > 0) {
            return accountType;
        } else {
            throw new RuntimeException("Failed to update account type: " + accountType);
        }
    }
    
    @Override
    public boolean deleteById(Integer id) {
        logger.debug("Deleting account type by ID: {}", id);
        int rowsAffected = update(DELETE_SQL, id);
        return rowsAffected > 0;
    }
    
    @Override
    public long count() {
        logger.debug("Counting all account types");
        return count(COUNT_SQL);
    }
    
    @Override
    public boolean existsByName(String name) {
        logger.debug("Checking if account type exists by name: {}", name);
        return count(EXISTS_BY_NAME_SQL, name) > 0;
    }
    
    @Override
    protected AccountType mapRow(ResultSet resultSet) throws SQLException {
        AccountType accountType = new AccountType();
        accountType.setId(resultSet.getInt("id"));
        accountType.setName(resultSet.getString("name"));
        accountType.setDescription(resultSet.getString("description"));
        return accountType;
    }
} 