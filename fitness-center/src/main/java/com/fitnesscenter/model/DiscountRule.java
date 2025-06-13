package com.fitnesscenter.model;

import java.math.BigDecimal;

/**
 * Discount rule entity for determining discounts based on 
 * account type and number of completed cycles
 */
public class DiscountRule {
    private Integer id;
    private String name;
    private AccountType accountType; // Associated account type
    private Integer minCompletedCycles; // Minimum completed cycles for eligibility
    private BigDecimal discountPercentage; // Discount percentage (0-100)
    private boolean active;
    
    public DiscountRule() {
    }
    
    public DiscountRule(Integer id, String name, AccountType accountType, 
                     Integer minCompletedCycles, BigDecimal discountPercentage, boolean active) {
        this.id = id;
        this.name = name;
        this.accountType = accountType;
        this.minCompletedCycles = minCompletedCycles;
        this.discountPercentage = discountPercentage;
        this.active = active;
    }
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public AccountType getAccountType() {
        return accountType;
    }
    
    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }
    
    public Integer getMinCompletedCycles() {
        return minCompletedCycles;
    }
    
    public void setMinCompletedCycles(Integer minCompletedCycles) {
        this.minCompletedCycles = minCompletedCycles;
    }
    
    public BigDecimal getDiscountPercentage() {
        return discountPercentage;
    }
    
    public void setDiscountPercentage(BigDecimal discountPercentage) {
        this.discountPercentage = discountPercentage;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    /**
     * Check if this rule applies to the given account type and completed cycles
     * 
     * @param accountTypeId Account type ID
     * @param completedCycles Number of completed cycles
     * @return true if the rule applies, false otherwise
     */
    public boolean appliesTo(Integer accountTypeId, Integer completedCycles) {
        if (!active) {
            return false;
        }
        
        boolean accountTypeMatches = (accountType != null && accountType.getId().equals(accountTypeId));
        boolean cyclesMatch = completedCycles >= minCompletedCycles;
        
        return accountTypeMatches && cyclesMatch;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        DiscountRule that = (DiscountRule) o;
        
        return id != null ? id.equals(that.id) : that.id == null;
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
    
    @Override
    public String toString() {
        return "DiscountRule{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", accountType=" + accountType +
                ", minCompletedCycles=" + minCompletedCycles +
                ", discountPercentage=" + discountPercentage +
                ", active=" + active +
                '}';
    }
} 