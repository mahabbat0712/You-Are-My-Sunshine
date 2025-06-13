package com.fitnesscenter.dao;

import com.fitnesscenter.model.Assignment;

import java.util.List;
import java.util.Optional;

/**
 * DAO interface for assignment data access
 */
public interface AssignmentDao {

    /**
     * Find assignment by ID
     *
     * @param id Assignment ID
     * @return Optional containing assignment if found
     */
    Optional<Assignment> findById(Long id);
    
    /**
     * Find all assignments
     *
     * @return List of all assignments
     */
    List<Assignment> findAll();
    
    /**
     * Find assignments by trainer ID
     *
     * @param trainerId Trainer ID
     * @return List of assignments for trainer
     */
    List<Assignment> findByTrainerId(Long trainerId);
    
    /**
     * Find assignments by client ID
     *
     * @param clientId Client ID
     * @return List of assignments for client
     */
    List<Assignment> findByClientId(Long clientId);
    
    /**
     * Find assignments by order ID
     *
     * @param orderId Order ID
     * @return List of assignments for order
     */
    List<Assignment> findByOrderId(Long orderId);
    
    /**
     * Save assignment
     *
     * @param assignment Assignment to save
     * @return Saved assignment with ID
     */
    Assignment save(Assignment assignment);
    
    /**
     * Update assignment
     *
     * @param assignment Assignment to update
     * @return True if updated successfully
     */
    boolean update(Assignment assignment);
    
    /**
     * Delete assignment
     *
     * @param id Assignment ID
     * @return True if deleted successfully
     */
    boolean delete(Long id);
} 