package com.fitnesscenter.service;

import com.fitnesscenter.model.User;
import java.util.List;
import java.util.Optional;

/**
 * Service for trainer-related operations
 */
public interface TrainerService {
    /**
     * Find all trainers with pagination
     *
     * @param page Page number (1-based)
     * @param pageSize Page size
     * @return List of trainers
     */
    List<User> findAllTrainers(int page, int pageSize);
    
    /**
     * Find trainer by ID
     *
     * @param id Trainer ID
     * @return Optional containing trainer if found
     */
    Optional<User> findTrainerById(Long id);
    
    /**
     * Count total number of trainers
     *
     * @return Total number of trainers
     */
    int countTrainers();
} 