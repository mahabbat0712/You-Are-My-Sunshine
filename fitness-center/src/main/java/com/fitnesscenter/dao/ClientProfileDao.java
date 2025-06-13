package com.fitnesscenter.dao;

import com.fitnesscenter.model.ClientProfile;

import java.util.List;
import java.util.Optional;

/**
 * DAO interface for ClientProfile entity
 */
public interface ClientProfileDao extends BaseDao<ClientProfile, Long> {
    
    /**
     * Find client profile by user ID
     *
     * @param userId User ID
     * @return Optional with client profile if found, empty optional otherwise
     */
    Optional<ClientProfile> findByUserId(Long userId);
    
    /**
     * Find client profiles by account type
     *
     * @param accountTypeId Account type ID
     * @return List of client profiles with the given account type
     */
    List<ClientProfile> findByAccountType(Integer accountTypeId);
    
    /**
     * Find client profiles by account type with pagination
     *
     * @param accountTypeId Account type ID
     * @param offset Offset (0-based)
     * @param limit Maximum number of items to return
     * @return List of client profiles with the given account type
     */
    List<ClientProfile> findByAccountType(Integer accountTypeId, int offset, int limit);
    
    /**
     * Update account type for a client
     *
     * @param userId User ID
     * @param accountTypeId Account type ID
     * @return true if updated successfully, false otherwise
     */
    boolean updateAccountType(Long userId, Integer accountTypeId);
    
    /**
     * Update completed cycles for a client
     *
     * @param userId User ID
     * @param completedCycles New number of completed cycles
     * @return true if updated successfully, false otherwise
     */
    boolean updateCompletedCycles(Long userId, Integer completedCycles);
    
    /**
     * Increment completed cycles for a client by 1
     *
     * @param userId User ID
     * @return New number of completed cycles, or null if update failed
     */
    Integer incrementCompletedCycles(Long userId);
    
    /**
     * Count clients by account type
     *
     * @param accountTypeId Account type ID
     * @return Number of clients with the given account type
     */
    long countByAccountType(Integer accountTypeId);
} 