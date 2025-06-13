package com.fitnesscenter.service;

import com.fitnesscenter.model.AccountType;
import com.fitnesscenter.model.DiscountRule;
import com.fitnesscenter.model.User;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing discount rules and applying discounts
 */
public interface DiscountService {
    
    /**
     * Find all account types
     *
     * @return List of all account types
     */
    List<AccountType> findAllAccountTypes();
    
    /**
     * Find account type by ID
     *
     * @param id Account type ID
     * @return Optional with account type if found, empty optional otherwise
     */
    Optional<AccountType> findAccountTypeById(Integer id);
    
    /**
     * Find account type by name
     *
     * @param name Account type name
     * @return Optional with account type if found, empty optional otherwise
     */
    Optional<AccountType> findAccountTypeByName(String name);
    
    /**
     * Find all discount rules
     *
     * @return List of all discount rules
     */
    List<DiscountRule> findAllDiscountRules();
    
    /**
     * Find discount rule by ID
     *
     * @param id Discount rule ID
     * @return Optional with discount rule if found, empty optional otherwise
     */
    Optional<DiscountRule> findDiscountRuleById(Integer id);
    
    /**
     * Find discount rules by account type
     *
     * @param accountTypeId Account type ID
     * @return List of discount rules for the given account type
     */
    List<DiscountRule> findDiscountRulesByAccountType(Integer accountTypeId);
    
    /**
     * Find active discount rules by account type
     *
     * @param accountTypeId Account type ID
     * @return List of active discount rules for the given account type
     */
    List<DiscountRule> findActiveDiscountRulesByAccountType(Integer accountTypeId);
    
    /**
     * Save or update a discount rule
     *
     * @param discountRule Discount rule to save or update
     * @return Updated discount rule
     */
    DiscountRule saveDiscountRule(DiscountRule discountRule);
    
    /**
     * Delete a discount rule by ID
     *
     * @param id Discount rule ID
     * @return true if deleted successfully, false otherwise
     */
    boolean deleteDiscountRule(Integer id);
    
    /**
     * Activate or deactivate a discount rule
     *
     * @param id Discount rule ID
     * @param active true to activate, false to deactivate
     * @return true if updated successfully, false otherwise
     */
    boolean setDiscountRuleActive(Integer id, boolean active);
    
    /**
     * Calculate applicable discount percentage for a client
     *
     * @param clientId Client user ID
     * @return Discount percentage (0-100) as BigDecimal, or zero if no discount applies
     */
    BigDecimal calculateDiscountForClient(Long clientId);
    
    /**
     * Apply calculated discount to a price
     *
     * @param originalPrice Original price
     * @param clientId Client user ID
     * @return Tuple with [discount percentage, final price]
     */
    BigDecimal[] applyDiscount(BigDecimal originalPrice, Long clientId);
    
    /**
     * Update client account type
     *
     * @param userId User ID
     * @param accountTypeId Account type ID
     * @return true if updated successfully, false otherwise
     */
    boolean updateClientAccountType(Long userId, Integer accountTypeId);
} 