package com.fitnesscenter.dao;

import com.fitnesscenter.model.TrainingCycle;

import java.util.List;

/**
 * DAO interface for TrainingCycle entity
 */
public interface TrainingCycleDao extends BaseDao<TrainingCycle, Long> {
    
    /**
     * Find active training cycles
     *
     * @return List of active training cycles
     */
    List<TrainingCycle> findActive();
    
    /**
     * Find active training cycles with pagination
     *
     * @param offset Offset (0-based)
     * @param limit Maximum number of items to return
     * @return List of active training cycles
     */
    List<TrainingCycle> findActive(int offset, int limit);
    
    /**
     * Activate training cycle
     *
     * @param id Training cycle ID
     * @return true if activated successfully, false otherwise
     */
    boolean activate(Long id);
    
    /**
     * Deactivate training cycle
     *
     * @param id Training cycle ID
     * @return true if deactivated successfully, false otherwise
     */
    boolean deactivate(Long id);
} 