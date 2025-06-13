package com.fitnesscenter.service;

import com.fitnesscenter.model.Equipment;

import java.util.List;
import java.util.Set;

/**
 * Service interface for managing equipment
 */
public interface EquipmentService {
    
    /**
     * Find all equipment
     *
     * @return List of all equipment
     */
    List<Equipment> findAllEquipment();
    
    /**
     * Find equipment by ID
     *
     * @param id Equipment ID
     * @return Equipment if found, null otherwise
     */
    Equipment findEquipmentById(Long id);
    
    /**
     * Find equipment by multiple IDs
     *
     * @param ids List of equipment IDs
     * @return Set of equipment matching the IDs
     */
    Set<Equipment> findEquipmentByIds(List<Long> ids);
    
    /**
     * Create new equipment
     *
     * @param equipment Equipment to create
     * @return Created equipment
     */
    Equipment createEquipment(Equipment equipment);
    
    /**
     * Update equipment
     *
     * @param equipment Equipment to update
     * @return Updated equipment
     */
    Equipment updateEquipment(Equipment equipment);
    
    /**
     * Delete equipment
     *
     * @param id Equipment ID
     * @return true if deleted successfully, false otherwise
     */
    boolean deleteEquipment(Long id);
} 