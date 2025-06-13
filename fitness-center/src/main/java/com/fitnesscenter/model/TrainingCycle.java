package com.fitnesscenter.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * TrainingCycle entity representing training programs offered by the center
 */
public class TrainingCycle {
    private Long id;
    
    @NotBlank(message = "{validation.trainingcycle.name.required}")
    @Size(min = 3, max = 100, message = "{validation.trainingcycle.name.size}")
    private String name;
    
    @NotBlank(message = "{validation.trainingcycle.description.required}")
    private String description;
    
    @Positive(message = "{validation.trainingcycle.duration.positive}")
    private int durationDays;
    
    @Positive(message = "{validation.trainingcycle.price.positive}")
    private BigDecimal price;
    
    private BigDecimal discountedPrice; // Price after applying any applicable discounts
    
    private String difficulty; // Сложность программы: "Начальный", "Средний", "Продвинутый"
    
    private Integer sessionsPerWeek; // Количество тренировок в неделю
    
    private boolean active;
    
    private LocalDateTime createdAt;
    
    // Constructors
    public TrainingCycle() {
        this.active = true;
        this.createdAt = LocalDateTime.now();
    }
    
    public TrainingCycle(String name, String description, int durationDays, BigDecimal price) {
        this();
        this.name = name;
        this.description = description;
        this.durationDays = durationDays;
        this.price = price;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public int getDurationDays() {
        return durationDays;
    }
    
    public void setDurationDays(int durationDays) {
        this.durationDays = durationDays;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public BigDecimal getDiscountedPrice() {
        return discountedPrice != null ? discountedPrice : price;
    }
    
    public void setDiscountedPrice(BigDecimal discountedPrice) {
        this.discountedPrice = discountedPrice;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    /**
     * Calculate duration in weeks
     *
     * @return Duration in weeks (rounded up)
     */
    public int getDurationWeeks() {
        return (int) Math.ceil(durationDays / 7.0);
    }
    
    public String getDifficulty() {
        return difficulty;
    }
    
    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }
    
    public Integer getSessionsPerWeek() {
        return sessionsPerWeek;
    }
    
    public void setSessionsPerWeek(Integer sessionsPerWeek) {
        this.sessionsPerWeek = sessionsPerWeek;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrainingCycle that = (TrainingCycle) o;
        return id != null && Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "TrainingCycle{" +
               "id=" + id +
               ", name='" + name + '\'' +
               ", durationDays=" + durationDays +
               ", price=" + price +
               ", active=" + active +
               '}';
    }
} 