package com.fitnesscenter.service;

import com.fitnesscenter.model.TrainingCycle;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing training cycles
 */
public interface TrainingCycleService {
    
    /**
     * Find training cycle by ID
     *
     * @param id Training cycle ID
     * @return Optional with training cycle if found, empty optional otherwise
     */
    Optional<TrainingCycle> findById(Long id);
    
    /**
     * Find all training cycles
     *
     * @return List of all training cycles
     */
    List<TrainingCycle> findAll();
    
    /**
     * Find training cycles with pagination
     *
     * @param page Page number (1-based)
     * @param size Page size
     * @return List of training cycles on the specified page
     */
    List<TrainingCycle> findAll(int page, int size);
    
    /**
     * Find active training cycles
     *
     * @return List of active training cycles
     */
    List<TrainingCycle> findActive();
    
    /**
     * Find active training cycles with pagination
     *
     * @param page Page number (1-based)
     * @param size Page size
     * @return List of active training cycles on the specified page
     */
    List<TrainingCycle> findActive(int page, int size);
    
    /**
     * Create a new training cycle
     *
     * @param trainingCycle Training cycle to create
     * @return Created training cycle with ID
     */
    TrainingCycle createTrainingCycle(TrainingCycle trainingCycle);
    
    /**
     * Update an existing training cycle
     *
     * @param trainingCycle Training cycle to update
     * @return Updated training cycle
     */
    TrainingCycle updateTrainingCycle(TrainingCycle trainingCycle);
    
    /**
     * Activate training cycle
     *
     * @param id Training cycle ID
     * @return true if activated successfully, false otherwise
     */
    boolean activateTrainingCycle(Long id);
    
    /**
     * Deactivate training cycle
     *
     * @param id Training cycle ID
     * @return true if deactivated successfully, false otherwise
     */
    boolean deactivateTrainingCycle(Long id);
    
    /**
     * Delete training cycle
     *
     * @param id Training cycle ID
     * @return true if deleted successfully, false otherwise
     */
    boolean deleteTrainingCycle(Long id);
    
    /**
     * Count total number of training cycles
     *
     * @return Total number of training cycles
     */
    long countTrainingCycles();
} 