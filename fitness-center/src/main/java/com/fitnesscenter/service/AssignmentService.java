package com.fitnesscenter.service;

import com.fitnesscenter.model.Assignment;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing assignments
 */
public interface AssignmentService {

    /**
     * Find assignment by ID
     *
     * @param id Assignment ID
     * @return Optional containing assignment if found
     */
    Optional<Assignment> findAssignmentById(Long id);
    
    /**
     * Find all assignments
     *
     * @return List of all assignments
     */
    List<Assignment> findAllAssignments();
    
    /**
     * Find assignments by trainer ID
     *
     * @param trainerId Trainer ID
     * @return List of assignments for trainer
     */
    List<Assignment> findAssignmentsByTrainer(Long trainerId);
    
    /**
     * Find assignments by client ID
     *
     * @param clientId Client ID
     * @return List of assignments for client
     */
    List<Assignment> findAssignmentsByClient(Long clientId);
    
    /**
     * Find assignments by order ID
     *
     * @param orderId Order ID
     * @return List of assignments for order
     */
    List<Assignment> findAssignmentsByOrder(Long orderId);
    
    /**
     * Create new assignment
     *
     * @param assignment Assignment to create
     * @return Created assignment
     */
    Assignment createAssignment(Assignment assignment);
    
    /**
     * Save assignment
     *
     * @param assignment Assignment to save
     * @return Saved assignment
     */
    Assignment saveAssignment(Assignment assignment);
    
    /**
     * Update assignment
     *
     * @param assignment Assignment to update
     * @return Updated assignment
     */
    Assignment updateAssignment(Assignment assignment);
    
    /**
     * Update assignment status
     *
     * @param id Assignment ID
     * @param status New status
     * @return True if updated successfully
     */
    boolean updateAssignmentStatus(Long id, String status);
    
    /**
     * Delete assignment
     *
     * @param id Assignment ID
     * @return True if deleted successfully
     */
    boolean deleteAssignment(Long id);
} 