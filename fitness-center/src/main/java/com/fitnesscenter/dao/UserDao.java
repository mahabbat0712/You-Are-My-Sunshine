package com.fitnesscenter.dao;

import com.fitnesscenter.model.ClientProfile;
import com.fitnesscenter.model.User;

import java.util.List;
import java.util.Optional;

/**
 * DAO interface for User entity
 */
public interface UserDao extends BaseDao<User, Long> {
    
    /**
     * Find user by username
     *
     * @param username Username to search for
     * @return Optional with user if found, empty optional otherwise
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Find user by email
     *
     * @param email Email to search for
     * @return Optional with user if found, empty optional otherwise
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Find users by role
     *
     * @param roleName Role name to search for
     * @return List of users with the specified role
     */
    List<User> findByRole(String roleName);
    
    /**
     * Find users by role with pagination
     *
     * @param roleName Role name to search for
     * @param offset Offset (0-based)
     * @param limit Maximum number of items to return
     * @return List of users with the specified role
     */
    List<User> findByRole(String roleName, int offset, int limit);
    
    /**
     * Add role to user
     *
     * @param userId User ID
     * @param roleId Role ID
     * @return true if added successfully, false otherwise
     */
    boolean addRoleToUser(Long userId, Integer roleId);
    
    /**
     * Remove role from user
     *
     * @param userId User ID
     * @param roleId Role ID
     * @return true if removed successfully, false otherwise
     */
    boolean removeRoleFromUser(Long userId, Integer roleId);
    
    /**
     * Check if username already exists
     *
     * @param username Username to check
     * @return true if username exists, false otherwise
     */
    boolean existsByUsername(String username);
    
    /**
     * Check if email already exists
     *
     * @param email Email to check
     * @return true if email exists, false otherwise
     */
    boolean existsByEmail(String email);
    
    /**
     * Count users by role
     *
     * @param roleName Role name to count
     * @return Number of users with the specified role
     */
    long countByRole(String roleName);
    
    /**
     * Find client profile by user ID
     *
     * @param userId User ID
     * @return Optional with client profile if found, empty optional otherwise
     */
    Optional<ClientProfile> findClientProfileByUserId(Long userId);
    
    /**
     * Save client profile
     *
     * @param clientProfile Client profile to save
     * @return Saved client profile
     */
    ClientProfile saveClientProfile(ClientProfile clientProfile);
} 