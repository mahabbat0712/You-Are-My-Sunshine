package com.fitnesscenter.service.impl;

import com.fitnesscenter.dao.RoleDao;
import com.fitnesscenter.dao.UserDao;
import com.fitnesscenter.model.AccountType;
import com.fitnesscenter.model.ClientProfile;
import com.fitnesscenter.model.Role;
import com.fitnesscenter.model.User;
import com.fitnesscenter.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import at.favre.lib.crypto.bcrypt.BCrypt;

/**
 * Implementation of UserService interface
 */
@Service
public class UserServiceImpl implements UserService {
    private static final Logger logger = LogManager.getLogger(UserServiceImpl.class);
    
    private final UserDao userDao;
    private final RoleDao roleDao;
    
    @Autowired
    public UserServiceImpl(UserDao userDao, RoleDao roleDao) {
        this.userDao = userDao;
        this.roleDao = roleDao;
    }
    
    @Override
    public Optional<User> findById(Long id) {
        logger.debug("Finding user by ID: {}", id);
        return userDao.findById(id);
    }
    
    @Override
    public Optional<User> findByUsername(String username) {
        logger.debug("Finding user by username: {}", username);
        return userDao.findByUsername(username);
    }
    
    @Override
    public Optional<User> findByEmail(String email) {
        logger.debug("Finding user by email: {}", email);
        return userDao.findByEmail(email);
    }
    
    @Override
    public List<User> findAll() {
        logger.debug("Finding all users");
        return userDao.findAll();
    }
    
    @Override
    public List<User> findAll(int page, int size) {
        logger.debug("Finding users with pagination: page={}, size={}", page, size);
        int offset = (page - 1) * size;
        return userDao.findAll(offset, size);
    }
    
    @Override
    public List<User> findByRole(String roleName) {
        logger.debug("Finding users by role: {}", roleName);
        return userDao.findByRole(roleName);
    }
    
    @Override
    public List<User> findByRole(String roleName, int page, int size) {
        logger.debug("Finding users by role with pagination: role={}, page={}, size={}", 
                roleName, page, size);
        int offset = (page - 1) * size;
        return userDao.findByRole(roleName, offset, size);
    }
    
    @Override
    public User createUser(User user, String rawPassword) {
        logger.debug("Creating new user: {}", user);
        
        // Check if username or email already exists
        if (userDao.existsByUsername(user.getUsername())) {
            logger.error("Username already exists: {}", user.getUsername());
            throw new IllegalArgumentException("Username already exists: " + user.getUsername());
        }
        
        if (userDao.existsByEmail(user.getEmail())) {
            logger.error("Email already exists: {}", user.getEmail());
            throw new IllegalArgumentException("Email already exists: " + user.getEmail());
        }
        
        // Hash the password
        String hashedPassword = hashPassword(rawPassword);
        user.setPassword(hashedPassword);
        
        // Save the user
        return userDao.save(user);
    }
    
    @Override
    public User updateUser(User user) {
        logger.debug("Updating user: {}", user);
        
        // Ensure user exists
        User existingUser = userDao.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + user.getId()));
        
        // Check if username is being changed and already exists
        if (!existingUser.getUsername().equals(user.getUsername()) && 
                userDao.existsByUsername(user.getUsername())) {
            logger.error("Username already exists: {}", user.getUsername());
            throw new IllegalArgumentException("Username already exists: " + user.getUsername());
        }
        
        // Check if email is being changed and already exists
        if (!existingUser.getEmail().equals(user.getEmail()) && 
                userDao.existsByEmail(user.getEmail())) {
            logger.error("Email already exists: {}", user.getEmail());
            throw new IllegalArgumentException("Email already exists: " + user.getEmail());
        }
        
        // Preserve the existing password if not provided in the update
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            user.setPassword(existingUser.getPassword());
        }
        
        // Save the updated user
        return userDao.save(user);
    }
    
    @Override
    public boolean changePassword(Long userId, String newPassword) {
        logger.debug("Changing password for user ID: {}", userId);
        
        // Find the user
        User user = userDao.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        
        // Hash the new password
        String hashedPassword = hashPassword(newPassword);
        user.setPassword(hashedPassword);
        
        // Save the user
        userDao.save(user);
        
        return true;
    }
    
    @Override
    public boolean changePassword(Long userId, String currentPassword, String newPassword) {
        logger.debug("Changing password with verification for user ID: {}", userId);
        
        // Find the user
        User user = userDao.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        
        // Verify current password
        BCrypt.Result result = BCrypt.verifyer().verify(currentPassword.toCharArray(), user.getPassword());
        if (!result.verified) {
            logger.warn("Current password verification failed for user ID: {}", userId);
            return false;
        }
        
        // Hash the new password
        String hashedPassword = hashPassword(newPassword);
        user.setPassword(hashedPassword);
        
        // Save the user
        userDao.save(user);
        logger.info("Password changed successfully for user ID: {}", userId);
        
        return true;
    }
    
    @Override
    public boolean deleteUser(Long id) {
        logger.debug("Deleting user by ID: {}", id);
        return userDao.deleteById(id);
    }
    
    @Override
    public boolean assignRole(Long userId, String roleName) {
        logger.debug("Assigning role {} to user {}", roleName, userId);
        
        // Find the user
        User user = userDao.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        
        // Find the role
        Role role = roleDao.findByName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));
        
        // Check if user already has this role
        if (user.hasRole(roleName)) {
            logger.info("User already has role {}: {}", roleName, userId);
            return true;
        }
        
        // Add role to user
        return userDao.addRoleToUser(userId, role.getId());
    }
    
    @Override
    public boolean removeRole(Long userId, String roleName) {
        logger.debug("Removing role {} from user {}", roleName, userId);
        
        // Find the user
        User user = userDao.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        
        // Find the role
        Role role = roleDao.findByName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));
        
        // Check if user has this role
        if (!user.hasRole(roleName)) {
            logger.info("User does not have role {}: {}", roleName, userId);
            return true;
        }
        
        // Remove role from user
        return userDao.removeRoleFromUser(userId, role.getId());
    }
    
    @Override
    public boolean usernameExists(String username) {
        logger.debug("Checking if username exists: {}", username);
        return userDao.existsByUsername(username);
    }
    
    @Override
    public boolean emailExists(String email) {
        logger.debug("Checking if email exists: {}", email);
        return userDao.existsByEmail(email);
    }
    
    @Override
    public long countUsers() {
        logger.debug("Counting all users");
        return userDao.count();
    }
    
    @Override
    public long countByRole(String roleName) {
        logger.debug("Counting users by role: {}", roleName);
        return userDao.countByRole(roleName);
    }
    
    @Override
    public Optional<User> authenticate(String username, String password) {
        logger.debug("Authenticating user: {}", username);
        
        // Find the user
        Optional<User> userOpt = userDao.findByUsername(username);
        if (userOpt.isEmpty()) {
            logger.info("Authentication failed: user not found - {}", username);
            return Optional.empty();
        }
        
        User user = userOpt.get();
        
        // Verify password
        BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword());
        
        if (result.verified) {
            logger.info("Authentication successful for user: {}", username);
            return Optional.of(user);
        } else {
            logger.info("Authentication failed: invalid password for user - {}", username);
            return Optional.empty();
        }
    }
    
    @Override
    public Optional<ClientProfile> getClientProfile(Long userId) {
        logger.debug("Getting client profile for user ID: {}", userId);
        return userDao.findClientProfileByUserId(userId);
    }
    
    @Override
    public ClientProfile saveClientProfile(ClientProfile clientProfile) {
        Long userId = clientProfile.getUser() != null ? clientProfile.getUser().getId() : null;
        logger.debug("Saving client profile for user ID: {}", userId);
        return userDao.saveClientProfile(clientProfile);
    }
    
    @Override
    public User saveUser(User user) {
        logger.debug("Saving user: {}", user);
        return userDao.save(user);
    }
    
    @Override
    public List<User> findAllTrainers() {
        logger.debug("Finding all trainers");
        return userDao.findByRole("TRAINER");
    }
    
    @Override
    public boolean incrementClientCompletedCycles(Long clientId) {
        logger.debug("Incrementing completed cycles for client ID: {}", clientId);
        
        Optional<ClientProfile> profileOpt = userDao.findClientProfileByUserId(clientId);
        if (!profileOpt.isPresent()) {
            // Create new profile if it doesn't exist
            User user = findById(clientId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + clientId));
            
            ClientProfile newProfile = new ClientProfile();
            newProfile.setUser(user);
            newProfile.setCompletedCycles(1);
            
            // Fix: Set default account type to prevent NULL value in account_type_id
            AccountType defaultAccountType = new AccountType();
            defaultAccountType.setId(1); // Assuming 1 is the ID for the default/regular account type
            newProfile.setAccountType(defaultAccountType);
            
            userDao.saveClientProfile(newProfile);
            logger.info("Created new client profile with completed cycles=1 for user ID: {}", clientId);
            return true;
        }
        
        // Update existing profile
        ClientProfile profile = profileOpt.get();
        Integer currentCycles = profile.getCompletedCycles();
        if (currentCycles == null) {
            currentCycles = 0;
        }
        
        profile.setCompletedCycles(currentCycles + 1);
        userDao.saveClientProfile(profile);
        logger.info("Incremented completed cycles to {} for client ID: {}", 
                currentCycles + 1, clientId);
        
        return true;
    }
    
    @Override
    public long countUsersByRole(String role) {
        try {
            return countByRole(role);
        } catch (Exception e) {
            logger.error("Error counting users by role: {}", role, e);
            return 0;
        }
    }
    
    @Override
    public List<User> findAllUsers() {
        logger.debug("Finding all users (findAllUsers alias)");
        return findAll();
    }
    
    @Override
    public boolean roleExists(String roleName) {
        logger.debug("Checking if role exists: {}", roleName);
        try {
            return roleDao.findByName(roleName).isPresent();
        } catch (Exception e) {
            logger.error("Error checking if role exists: {}", roleName, e);
            return false;
        }
    }
    
    /**
     * Hash a password using BCrypt
     *
     * @param rawPassword Raw password to hash
     * @return BCrypt hashed password
     */
    private String hashPassword(String rawPassword) {
        return BCrypt.withDefaults().hashToString(12, rawPassword.toCharArray());
    }
} 