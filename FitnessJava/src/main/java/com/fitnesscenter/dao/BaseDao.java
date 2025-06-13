package com.fitnesscenter.dao;

import java.util.List;
import java.util.Optional;

/**
 * Base DAO interface for CRUD operations
 *
 * @param <T> Entity type
 * @param <ID> ID type
 */
public interface BaseDao<T, ID> {
    
    /**
     * Save entity (create or update)
     *
     * @param entity Entity to save
     * @return Saved entity with ID
     */
    T save(T entity);
    
    /**
     * Find entity by ID
     *
     * @param id Entity ID
     * @return Optional with entity if found, empty optional otherwise
     */
    Optional<T> findById(ID id);
    
    /**
     * Find all entities
     *
     * @return List of all entities
     */
    List<T> findAll();
    
    /**
     * Find entities with pagination
     *
     * @param offset Offset (0-based)
     * @param limit Maximum number of items to return
     * @return List of entities
     */
    List<T> findAll(int offset, int limit);
    
    /**
     * Delete entity by ID
     *
     * @param id Entity ID
     * @return true if deleted, false otherwise
     */
    boolean deleteById(ID id);
    
    /**
     * Count all entities
     *
     * @return Total number of entities
     */
    long count();
} 