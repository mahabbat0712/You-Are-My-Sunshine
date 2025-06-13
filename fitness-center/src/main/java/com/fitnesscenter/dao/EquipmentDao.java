package com.fitnesscenter.dao;

import com.fitnesscenter.model.Equipment;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * DAO interface for equipment data access
 */
public interface EquipmentDao {

    /**
     * Find equipment by ID
     *
     * @param id Equipment ID
     * @return Optional containing equipment if found
     */
    Optional<Equipment> findById(Long id);
    
    /**
     * Find all equipment
     *
     * @return List of all equipment
     */
    List<Equipment> findAll();
    
    /**
     * Find equipment by IDs
     *
     * @param ids List of equipment IDs
     * @return Set of equipment
     */
    Set<Equipment> findByIds(List<Long> ids);
    
    /**
     * Save equipment
     *
     * @param equipment Equipment to save
     * @return Saved equipment with ID
     */
    Equipment save(Equipment equipment);
    
    /**
     * Update equipment
     *
     * @param equipment Equipment to update
     * @return True if updated successfully
     */
    boolean update(Equipment equipment);
    
    /**
     * Delete equipment
     *
     * @param id Equipment ID
     * @return True if deleted successfully
     */
    boolean delete(Long id);
} 