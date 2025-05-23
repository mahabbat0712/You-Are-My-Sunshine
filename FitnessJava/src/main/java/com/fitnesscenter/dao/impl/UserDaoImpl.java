package com.fitnesscenter.dao.impl;

import com.fitnesscenter.dao.AbstractDao;
import com.fitnesscenter.dao.UserDao;
import com.fitnesscenter.model.Role;
import com.fitnesscenter.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Implementation of UserDao interface using JDBC
 */
@Repository
public class UserDaoImpl extends AbstractDao<User, Long> implements UserDao {
    private static final Logger logger = LogManager.getLogger(UserDaoImpl.class);
    
    private static final String FIND_BY_ID_SQL = 
            "SELECT u.id, u.username, u.password, u.email, u.first_name, u.last_name, u.phone, " +
            "u.created_at, u.active FROM users u WHERE u.id = ?";
    
    private static final String FIND_BY_USERNAME_SQL = 
            "SELECT u.id, u.username, u.password, u.email, u.first_name, u.last_name, u.phone, " +
            "u.created_at, u.active FROM users u WHERE u.username = ?";
    
    private static final String FIND_BY_EMAIL_SQL = 
            "SELECT u.id, u.username, u.password, u.email, u.first_name, u.last_name, u.phone, " +
            "u.created_at, u.active FROM users u WHERE u.email = ?";
    
    private static final String FIND_ALL_SQL = 
            "SELECT u.id, u.username, u.password, u.email, u.first_name, u.last_name, u.phone, " +
            "u.created_at, u.active FROM users u ORDER BY u.id";
    
    private static final String FIND_ALL_PAGINATED_SQL = 
            "SELECT u.id, u.username, u.password, u.email, u.first_name, u.last_name, u.phone, " +
            "u.created_at, u.active FROM users u ORDER BY u.id LIMIT ? OFFSET ?";
    
    private static final String FIND_BY_ROLE_SQL = 
            "SELECT u.id, u.username, u.password, u.email, u.first_name, u.last_name, u.phone, " +
            "u.created_at, u.active FROM users u " +
            "JOIN user_roles ur ON u.id = ur.user_id " +
            "JOIN roles r ON ur.role_id = r.id " +
            "WHERE r.name = ? ORDER BY u.id";
    
    private static final String FIND_BY_ROLE_PAGINATED_SQL = 
            "SELECT u.id, u.username, u.password, u.email, u.first_name, u.last_name, u.phone, " +
            "u.created_at, u.active FROM users u " +
            "JOIN user_roles ur ON u.id = ur.user_id " +
            "JOIN roles r ON ur.role_id = r.id " +
            "WHERE r.name = ? ORDER BY u.id LIMIT ? OFFSET ?";
    
    private static final String FIND_ROLES_FOR_USER_SQL = 
            "SELECT r.id, r.name FROM roles r " +
            "JOIN user_roles ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = ?";
    
    private static final String INSERT_SQL = 
            "INSERT INTO users (username, password, email, first_name, last_name, phone, created_at, active) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    
    private static final String UPDATE_SQL = 
            "UPDATE users SET username = ?, password = ?, email = ?, first_name = ?, " +
            "last_name = ?, phone = ?, active = ? WHERE id = ?";
    
    private static final String DELETE_SQL = 
            "DELETE FROM users WHERE id = ?";
    
    private static final String COUNT_SQL = 
            "SELECT COUNT(*) FROM users";
    
    private static final String COUNT_BY_ROLE_SQL = 
            "SELECT COUNT(*) FROM users u " +
            "JOIN user_roles ur ON u.id = ur.user_id " +
            "JOIN roles r ON ur.role_id = r.id " +
            "WHERE r.name = ?";
    
    private static final String EXISTS_BY_USERNAME_SQL = 
            "SELECT COUNT(*) FROM users WHERE username = ?";
    
    private static final String EXISTS_BY_EMAIL_SQL = 
            "SELECT COUNT(*) FROM users WHERE email = ?";
    
    private static final String ADD_ROLE_TO_USER_SQL = 
            "INSERT INTO user_roles (user_id, role_id) VALUES (?, ?)";
    
    private static final String REMOVE_ROLE_FROM_USER_SQL = 
            "DELETE FROM user_roles WHERE user_id = ? AND role_id = ?";
    
    @Override
    public Optional<User> findById(Long id) {
        logger.debug("Finding user by ID: {}", id);
        Optional<User> user = findOne(FIND_BY_ID_SQL, id);
        
        user.ifPresent(this::loadRoles);
        
        return user;
    }
    
    @Override
    public Optional<User> findByUsername(String username) {
        logger.debug("Finding user by username: {}", username);
        Optional<User> user = findOne(FIND_BY_USERNAME_SQL, username);
        
        user.ifPresent(this::loadRoles);
        
        return user;
    }
    
    @Override
    public Optional<User> findByEmail(String email) {
        logger.debug("Finding user by email: {}", email);
        Optional<User> user = findOne(FIND_BY_EMAIL_SQL, email);
        
        user.ifPresent(this::loadRoles);
        
        return user;
    }
    
    @Override
    public List<User> findAll() {
        logger.debug("Finding all users");
        List<User> users = findMany(FIND_ALL_SQL);
        
        users.forEach(this::loadRoles);
        
        return users;
    }
    
    @Override
    public List<User> findAll(int offset, int limit) {
        logger.debug("Finding users with pagination: offset={}, limit={}", offset, limit);
        List<User> users = findMany(FIND_ALL_PAGINATED_SQL, limit, offset);
        
        users.forEach(this::loadRoles);
        
        return users;
    }
    
    @Override
    public List<User> findByRole(String roleName) {
        logger.debug("Finding users by role: {}", roleName);
        List<User> users = findMany(FIND_BY_ROLE_SQL, roleName);
        
        users.forEach(this::loadRoles);
        
        return users;
    }
    
    @Override
    public List<User> findByRole(String roleName, int offset, int limit) {
        logger.debug("Finding users by role with pagination: role={}, offset={}, limit={}", 
                roleName, offset, limit);
        List<User> users = findMany(FIND_BY_ROLE_PAGINATED_SQL, roleName, limit, offset);
        
        users.forEach(this::loadRoles);
        
        return users;
    }
    
    @Override
    public User save(User user) {
        if (user.getId() == null) {
            return insert(user);
        } else {
            return update(user);
        }
    }
    
    private User insert(User user) {
        logger.debug("Inserting new user: {}", user);
        
        LocalDateTime createdAt = user.getCreatedAt() != null ? 
                user.getCreatedAt() : LocalDateTime.now();
        
        Optional<Long> generatedId = updateAndGetGeneratedKey(INSERT_SQL,
                user.getUsername(),
                user.getPassword(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                createdAt,
                user.isActive()
        );
        
        if (generatedId.isPresent()) {
            user.setId(generatedId.get());
            saveUserRoles(user);
            return user;
        } else {
            throw new RuntimeException("Failed to insert user: " + user);
        }
    }
    
    private User update(User user) {
        logger.debug("Updating user: {}", user);
        
        int rowsAffected = update(UPDATE_SQL,
                user.getUsername(),
                user.getPassword(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.isActive(),
                user.getId()
        );
        
        if (rowsAffected > 0) {
            saveUserRoles(user);
            return user;
        } else {
            throw new RuntimeException("Failed to update user: " + user);
        }
    }
    
    private void saveUserRoles(User user) {
        // Implementation depends on how roles are managed
        // For simplicity, let's assume the roles set in the user object is the source of truth
        
        // In a real application, you'd compare existing roles with new roles and only add/remove the differences
        // but for now, let's keep it simple by removing all existing roles and adding the new ones
        
        if (user.getId() != null && user.getRoles() != null && !user.getRoles().isEmpty()) {
            Connection connection = null;
            try {
                connection = databaseManager.getConnection();
                beginTransaction(connection);
                
                // First, delete all existing roles for this user
                update("DELETE FROM user_roles WHERE user_id = ?", user.getId());
                
                // Then add all current roles
                for (Role role : user.getRoles()) {
                    if (role.getId() != null) {
                        update(ADD_ROLE_TO_USER_SQL, user.getId(), role.getId());
                    }
                }
                
                commitTransaction(connection);
            } catch (SQLException e) {
                rollbackTransaction(connection);
                logger.error("Error saving user roles for user: {}", user.getId(), e);
                throw new RuntimeException("Error saving user roles", e);
            } finally {
                if (connection != null) {
                    databaseManager.releaseConnection(connection);
                }
            }
        }
    }
    
    @Override
    public boolean deleteById(Long id) {
        logger.debug("Deleting user by ID: {}", id);
        int rowsAffected = update(DELETE_SQL, id);
        return rowsAffected > 0;
    }
    
    @Override
    public long count() {
        logger.debug("Counting all users");
        return count(COUNT_SQL);
    }
    
    @Override
    public long countByRole(String roleName) {
        logger.debug("Counting users by role: {}", roleName);
        return count(COUNT_BY_ROLE_SQL, roleName);
    }
    
    @Override
    public boolean existsByUsername(String username) {
        logger.debug("Checking if username exists: {}", username);
        return count(EXISTS_BY_USERNAME_SQL, username) > 0;
    }
    
    @Override
    public boolean existsByEmail(String email) {
        logger.debug("Checking if email exists: {}", email);
        return count(EXISTS_BY_EMAIL_SQL, email) > 0;
    }
    
    @Override
    public boolean addRoleToUser(Long userId, Integer roleId) {
        logger.debug("Adding role {} to user {}", roleId, userId);
        try {
            int rowsAffected = update(ADD_ROLE_TO_USER_SQL, userId, roleId);
            return rowsAffected > 0;
        } catch (Exception e) {
            logger.error("Error adding role to user", e);
            return false;
        }
    }
    
    @Override
    public boolean removeRoleFromUser(Long userId, Integer roleId) {
        logger.debug("Removing role {} from user {}", roleId, userId);
        int rowsAffected = update(REMOVE_ROLE_FROM_USER_SQL, userId, roleId);
        return rowsAffected > 0;
    }
    
    /**
     * Load roles for a user
     *
     * @param user User to load roles for
     */
    private void loadRoles(User user) {
        logger.debug("Loading roles for user: {}", user.getId());
        
        Connection connection = null;
        try {
            connection = databaseManager.getConnection();
            
            Set<Role> roles = new HashSet<>();
            
            try (var statement = prepareStatement(connection, FIND_ROLES_FOR_USER_SQL, user.getId());
                 var resultSet = statement.executeQuery()) {
                
                while (resultSet.next()) {
                    Role role = new Role();
                    role.setId(resultSet.getInt("id"));
                    role.setName(resultSet.getString("name"));
                    roles.add(role);
                }
            }
            
            user.setRoles(roles);
            
        } catch (SQLException e) {
            logger.error("Error loading roles for user: {}", user.getId(), e);
            throw new RuntimeException("Error loading user roles", e);
        } finally {
            if (connection != null) {
                databaseManager.releaseConnection(connection);
            }
        }
    }
    
    @Override
    protected User mapRow(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setId(resultSet.getLong("id"));
        user.setUsername(resultSet.getString("username"));
        user.setPassword(resultSet.getString("password"));
        user.setEmail(resultSet.getString("email"));
        user.setFirstName(resultSet.getString("first_name"));
        user.setLastName(resultSet.getString("last_name"));
        user.setPhone(resultSet.getString("phone"));
        user.setCreatedAt(resultSet.getTimestamp("created_at").toLocalDateTime());
        user.setActive(resultSet.getBoolean("active"));
        return user;
    }
} 