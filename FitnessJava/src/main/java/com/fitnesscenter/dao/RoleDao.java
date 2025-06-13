package com.fitnesscenter.dao;

import com.fitnesscenter.model.Role;

import java.util.Optional;

/**
 * DAO interface for Role entity
 */
public interface RoleDao extends BaseDao<Role, Integer> {
    
    /**
     * Find role by name
     *
     * @param name Role name to search for
     * @return Optional with role if found, empty optional otherwise
     */
    Optional<Role> findByName(String name);
    
    /**
     * Check if role name exists
     *
     * @param name Role name to check
     * @return true if role name exists, false otherwise
     */
    boolean existsByName(String name);
} 