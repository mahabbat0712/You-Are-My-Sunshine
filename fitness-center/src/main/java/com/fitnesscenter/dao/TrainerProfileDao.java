package com.fitnesscenter.dao;

import com.fitnesscenter.model.TrainerProfile;
import java.util.Optional;

/**
 * Data access object for TrainerProfile entities
 */
public interface TrainerProfileDao {
    /**
     * Find trainer profile by user ID
     *
     * @param userId User ID
     * @return Optional with trainer profile if found, empty optional otherwise
     */
    Optional<TrainerProfile> findByUserId(Long userId);
    
    /**
     * Save trainer profile
     *
     * @param trainerProfile Trainer profile to save
     * @return Saved trainer profile
     */
    TrainerProfile save(TrainerProfile trainerProfile);
} 