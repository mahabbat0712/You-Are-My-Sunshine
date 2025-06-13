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
    
    /**
     * Find all active training cycles
     * 
     * @return List of active training cycles
     */
    List<TrainingCycle> findActiveTrainingCycles();
    
    /**
     * Find training cycle by ID
     * 
     * @param id Training cycle ID
     * @return Optional with training cycle if found, empty optional otherwise
     */
    Optional<TrainingCycle> findTrainingCycleById(Long id);
    
    /**
     * Find all training cycles (alias for findAll)
     * 
     * @return List of all training cycles
     */
    List<TrainingCycle> findAllTrainingCycles();
    
    /**
     * Find training cycles by active status with pagination
     * 
     * @param active Whether to find active or inactive cycles
     * @param offset Pagination offset
     * @param limit Pagination limit
     * @return List of training cycles matching the criteria
     */
    List<TrainingCycle> findByActive(boolean active, int offset, int limit);
    
    /**
     * Count training cycles by active status
     * 
     * @param active Whether to count active or inactive cycles
     * @return Count of training cycles matching the criteria
     */
    long countByActive(boolean active);
    
    /**
     * Search training cycles by query string with pagination
     * 
     * @param query Search query
     * @param offset Pagination offset
     * @param limit Pagination limit
     * @return List of training cycles matching the search criteria
     */
    List<TrainingCycle> search(String query, int offset, int limit);
    
    /**
     * Count training cycles matching search criteria
     * 
     * @param query Search query
     * @return Count of training cycles matching the search criteria
     */
    long countBySearch(String query);
} 