package com.fitnesscenter.service;

import com.fitnesscenter.model.ClientProfile;
import com.fitnesscenter.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing users
 */
public interface UserService {
    
    /**
     * Find user by ID
     *
     * @param id User ID
     * @return Optional with user if found, empty optional otherwise
     */
    Optional<User> findById(Long id);
    
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
     * Find all users
     *
     * @return List of all users
     */
    List<User> findAll();
    
    /**
     * Find users with pagination
     *
     * @param page Page number (1-based)
     * @param size Page size
     * @return List of users on the specified page
     */
    List<User> findAll(int page, int size);
    
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
     * @param page Page number (1-based)
     * @param size Page size
     * @return List of users with the specified role on the specified page
     */
    List<User> findByRole(String roleName, int page, int size);
    
    /**
     * Create a new user
     *
     * @param user User to create
     * @param rawPassword Raw password to hash
     * @return Created user with ID
     */
    User createUser(User user, String rawPassword);
    
    /**
     * Update an existing user
     *
     * @param user User to update
     * @return Updated user
     */
    User updateUser(User user);
    
    /**
     * Change user password
     *
     * @param userId User ID
     * @param newPassword New raw password to hash
     * @return true if password changed successfully, false otherwise
     */
    boolean changePassword(Long userId, String newPassword);
    
    /**
     * Change user password with current password verification
     *
     * @param userId User ID
     * @param currentPassword Current password for verification
     * @param newPassword New raw password to hash
     * @return true if password changed successfully, false otherwise
     */
    boolean changePassword(Long userId, String currentPassword, String newPassword);
    
    /**
     * Delete user by ID
     *
     * @param id User ID
     * @return true if deleted successfully, false otherwise
     */
    boolean deleteUser(Long id);
    
    /**
     * Assign role to user
     *
     * @param userId User ID
     * @param roleName Role name to assign
     * @return true if role assigned successfully, false otherwise
     */
    boolean assignRole(Long userId, String roleName);
    
    /**
     * Remove role from user
     *
     * @param userId User ID
     * @param roleName Role name to remove
     * @return true if role removed successfully, false otherwise
     */
    boolean removeRole(Long userId, String roleName);
    
    /**
     * Check if username already exists
     *
     * @param username Username to check
     * @return true if username exists, false otherwise
     */
    boolean usernameExists(String username);
    
    /**
     * Check if email already exists
     *
     * @param email Email to check
     * @return true if email exists, false otherwise
     */
    boolean emailExists(String email);
    
    /**
     * Count total number of users
     *
     * @return Total number of users
     */
    long countUsers();
    
    /**
     * Count users by role
     *
     * @param roleName Role name to count
     * @return Number of users with the specified role
     */
    long countByRole(String roleName);
    
    /**
     * Authenticate a user with username and password
     *
     * @param username Username
     * @param password Raw password
     * @return Optional with authenticated user if successful, empty optional otherwise
     */
    Optional<User> authenticate(String username, String password);
    
    /**
     * Get client profile for a user
     *
     * @param userId User ID
     * @return Optional with client profile if found, empty optional otherwise
     */
    Optional<ClientProfile> getClientProfile(Long userId);
    
    /**
     * Save client profile
     *
     * @param clientProfile Client profile to save
     * @return Saved client profile
     */
    ClientProfile saveClientProfile(ClientProfile clientProfile);
    
    /**
     * Save user
     *
     * @param user User to save
     * @return Saved user
     */
    User saveUser(User user);
    
    /**
     * Find all trainer users
     *
     * @return List of all users with TRAINER role
     */
    List<User> findAllTrainers();
    
    /**
     * Increment the completed cycles count for a client
     *
     * @param clientId Client user ID
     * @return true if incremented successfully, false otherwise
     */
    boolean incrementClientCompletedCycles(Long clientId);
    
    /**
     * Count users by role
     *
     * @param role Role name
     * @return Number of users with the specified role
     */
    long countUsersByRole(String role);
    
    /**
     * Find all users (alias for findAll)
     *
     * @return List of all users
     */
    List<User> findAllUsers();
    
    /**
     * Check if role exists
     *
     * @param roleName Role name to check
     * @return true if role exists, false otherwise
     */
    boolean roleExists(String roleName);
} 