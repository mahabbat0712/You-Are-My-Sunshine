package com.fitnesscenter.dao;

import com.fitnesscenter.model.DiscountRule;

import java.util.List;

/**
 * DAO interface for DiscountRule entity
 */
public interface DiscountRuleDao extends BaseDao<DiscountRule, Integer> {
    
    /**
     * Find discount rules by account type
     *
     * @param accountTypeId Account type ID
     * @return List of discount rules for the given account type
     */
    List<DiscountRule> findByAccountType(Integer accountTypeId);
    
    /**
     * Find active discount rules by account type
     *
     * @param accountTypeId Account type ID
     * @return List of active discount rules for the given account type
     */
    List<DiscountRule> findActiveByAccountType(Integer accountTypeId);
    
    /**
     * Find best applicable discount rule for the given account type and completed cycles
     * 
     * @param accountTypeId Account type ID
     * @param completedCycles Number of completed cycles
     * @return The best discount rule (highest discount percentage) or empty if none applies
     */
    DiscountRule findBestDiscount(Integer accountTypeId, Integer completedCycles);
    
    /**
     * Activate or deactivate a discount rule
     *
     * @param id Discount rule ID
     * @param active true to activate, false to deactivate
     * @return true if updated successfully, false otherwise
     */
    boolean setActive(Integer id, boolean active);
} 