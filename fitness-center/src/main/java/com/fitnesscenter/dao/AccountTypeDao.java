package com.fitnesscenter.dao;

import com.fitnesscenter.model.AccountType;

import java.util.Optional;

/**
 * DAO interface for AccountType entity
 */
public interface AccountTypeDao extends BaseDao<AccountType, Integer> {
    
    /**
     * Find account type by name
     *
     * @param name Account type name to search for
     * @return Optional with account type if found, empty optional otherwise
     */
    Optional<AccountType> findByName(String name);
    
    /**
     * Check if account type exists by name
     *
     * @param name Account type name to check
     * @return true if account type exists, false otherwise
     */
    boolean existsByName(String name);
} 