package com.fitnesscenter.model;

import java.time.LocalDate;

/**
 * Client profile with additional client-specific information
 * like account type, completed cycles, and health information
 */
public class ClientProfile {
    private Long id; // Database ID
    private User user; // Associated user
    private AccountType accountType; // Type of account (regular, corporate)
    private Integer completedCycles; // Number of completed training cycles
    private LocalDate dateOfBirth; // Client's date of birth
    private String healthNotes; // Health-related notes/restrictions
    
    public ClientProfile() {
        this.completedCycles = 0;
    }
    
    public ClientProfile(User user, AccountType accountType) {
        this.user = user;
        this.accountType = accountType;
        this.completedCycles = 0;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public AccountType getAccountType() {
        return accountType;
    }
    
    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }
    
    public Integer getCompletedCycles() {
        return completedCycles;
    }
    
    public void setCompletedCycles(Integer completedCycles) {
        this.completedCycles = completedCycles;
    }
    
    /**
     * Increment the number of completed cycles
     * 
     * @return Updated number of completed cycles
     */
    public Integer incrementCompletedCycles() {
        completedCycles++;
        return completedCycles;
    }
    
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }
    
    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
    
    public String getHealthNotes() {
        return healthNotes;
    }
    
    public void setHealthNotes(String healthNotes) {
        this.healthNotes = healthNotes;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        ClientProfile that = (ClientProfile) o;
        
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        return user != null ? user.equals(that.user) : that.user == null;
    }
    
    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (user != null ? user.hashCode() : 0);
        return result;
    }
    
    @Override
    public String toString() {
        return "ClientProfile{" +
                "id=" + id +
                ", user=" + (user != null ? user.getId() : null) +
                ", accountType=" + accountType +
                ", completedCycles=" + completedCycles +
                ", dateOfBirth=" + dateOfBirth +
                ", healthNotes='" + (healthNotes != null ? healthNotes.substring(0, Math.min(healthNotes.length(), 20)) + "..." : null) + '\'' +
                '}';
    }
} 