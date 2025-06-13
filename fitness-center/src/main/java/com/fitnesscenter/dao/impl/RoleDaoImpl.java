package com.fitnesscenter.dao.impl;

import com.fitnesscenter.dao.AbstractDao;
import com.fitnesscenter.dao.RoleDao;
import com.fitnesscenter.model.Role;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of RoleDao interface using JDBC
 */
@Repository
public class RoleDaoImpl extends AbstractDao<Role, Integer> implements RoleDao {
    private static final Logger logger = LogManager.getLogger(RoleDaoImpl.class);
    
    private static final String FIND_BY_ID_SQL = 
            "SELECT id, name FROM roles WHERE id = ?";
    
    private static final String FIND_BY_NAME_SQL = 
            "SELECT id, name FROM roles WHERE name = ?";
    
    private static final String FIND_ALL_SQL = 
            "SELECT id, name FROM roles ORDER BY id";
    
    private static final String FIND_ALL_PAGINATED_SQL = 
            "SELECT id, name FROM roles ORDER BY id LIMIT ? OFFSET ?";
    
    private static final String INSERT_SQL = 
            "INSERT INTO roles (name) VALUES (?)";
    
    private static final String UPDATE_SQL = 
            "UPDATE roles SET name = ? WHERE id = ?";
    
    private static final String DELETE_SQL = 
            "DELETE FROM roles WHERE id = ?";
    
    private static final String COUNT_SQL = 
            "SELECT COUNT(*) FROM roles";
    
    private static final String EXISTS_BY_NAME_SQL = 
            "SELECT COUNT(*) FROM roles WHERE name = ?";
    
    @Override
    public Optional<Role> findById(Integer id) {
        logger.debug("Finding role by ID: {}", id);
        return findOne(FIND_BY_ID_SQL, id);
    }
    
    @Override
    public Optional<Role> findByName(String name) {
        logger.debug("Finding role by name: {}", name);
        return findOne(FIND_BY_NAME_SQL, name);
    }
    
    @Override
    public List<Role> findAll() {
        logger.debug("Finding all roles");
        return findMany(FIND_ALL_SQL);
    }
    
    @Override
    public List<Role> findAll(int offset, int limit) {
        logger.debug("Finding roles with pagination: offset={}, limit={}", offset, limit);
        return findMany(FIND_ALL_PAGINATED_SQL, limit, offset);
    }
    
    @Override
    public Role save(Role role) {
        if (role.getId() == null) {
            return insert(role);
        } else {
            return update(role);
        }
    }
    
    private Role insert(Role role) {
        logger.debug("Inserting new role: {}", role);
        
        Optional<Long> generatedId = updateAndGetGeneratedKey(INSERT_SQL, role.getName());
        
        if (generatedId.isPresent()) {
            role.setId(generatedId.get().intValue());
            return role;
        } else {
            throw new RuntimeException("Failed to insert role: " + role);
        }
    }
    
    private Role update(Role role) {
        logger.debug("Updating role: {}", role);
        
        int rowsAffected = update(UPDATE_SQL, role.getName(), role.getId());
        
        if (rowsAffected > 0) {
            return role;
        } else {
            throw new RuntimeException("Failed to update role: " + role);
        }
    }
    
    @Override
    public boolean deleteById(Integer id) {
        logger.debug("Deleting role by ID: {}", id);
        int rowsAffected = update(DELETE_SQL, id);
        return rowsAffected > 0;
    }
    
    @Override
    public long count() {
        logger.debug("Counting all roles");
        return count(COUNT_SQL);
    }
    
    @Override
    public boolean existsByName(String name) {
        logger.debug("Checking if role name exists: {}", name);
        return count(EXISTS_BY_NAME_SQL, name) > 0;
    }
    
    @Override
    protected Role mapRow(ResultSet resultSet) throws SQLException {
        Role role = new Role();
        role.setId(resultSet.getInt("id"));
        role.setName(resultSet.getString("name"));
        return role;
    }
} 