package com.fitnesscenter.model;

import java.math.BigDecimal;

/**
 * Trainer profile with additional trainer-specific information
 */
public class TrainerProfile {
    private User user; // Associated user
    private String specialization; // Area of expertise
    private Integer experienceYears; // Years of experience
    private BigDecimal hourlyRate; // Hourly rate for training
    private String bio; // Trainer biography
    
    public TrainerProfile() {
        this.experienceYears = 0;
    }
    
    public TrainerProfile(User user) {
        this();
        this.user = user;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public String getSpecialization() {
        return specialization;
    }
    
    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }
    
    public Integer getExperienceYears() {
        return experienceYears;
    }
    
    public void setExperienceYears(Integer experienceYears) {
        this.experienceYears = experienceYears;
    }
    
    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }
    
    public void setHourlyRate(BigDecimal hourlyRate) {
        this.hourlyRate = hourlyRate;
    }
    
    public String getBio() {
        return bio;
    }
    
    public void setBio(String bio) {
        this.bio = bio;
    }
    
    @Override
    public String toString() {
        return "TrainerProfile{" +
                "user=" + (user != null ? user.getId() : null) +
                ", specialization='" + specialization + '\'' +
                ", experienceYears=" + experienceYears +
                ", hourlyRate=" + hourlyRate +
                '}';
    }
} 