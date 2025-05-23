package com.fitnesscenter.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Assignment entity representing trainer's assignments to clients
 */
public class Assignment {
    private Long id;
    
    @NotNull(message = "{validation.assignment.order.required}")
    private Order order;
    
    @NotNull(message = "{validation.assignment.category.required}")
    private AssignmentCategory category;
    
    @NotNull(message = "{validation.assignment.trainer.required}")
    private User trainer;
    
    @NotBlank(message = "{validation.assignment.title.required}")
    @Size(min = 3, max = 100, message = "{validation.assignment.title.size}")
    private String title;
    
    @NotBlank(message = "{validation.assignment.description.required}")
    private String description;
    
    private String schedule;
    
    private AssignmentStatus status;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private Set<Equipment> requiredEquipment = new HashSet<>();
    
    /**
     * Assignment status enum
     */
    public enum AssignmentStatus {
        ASSIGNED("ASSIGNED"),
        ACCEPTED("ACCEPTED"),
        REJECTED("REJECTED"),
        COMPLETED("COMPLETED");
        
        private final String value;
        
        AssignmentStatus(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
        
        public static AssignmentStatus fromValue(String value) {
            for (AssignmentStatus status : AssignmentStatus.values()) {
                if (status.value.equals(value)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Invalid assignment status: " + value);
        }
    }
    
    // Constructors
    public Assignment() {
        this.status = AssignmentStatus.ASSIGNED;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public Assignment(Order order, AssignmentCategory category, User trainer, String title, String description) {
        this();
        this.order = order;
        this.category = category;
        this.trainer = trainer;
        this.title = title;
        this.description = description;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Order getOrder() {
        return order;
    }
    
    public void setOrder(Order order) {
        this.order = order;
    }
    
    public AssignmentCategory getCategory() {
        return category;
    }
    
    public void setCategory(AssignmentCategory category) {
        this.category = category;
    }
    
    public User getTrainer() {
        return trainer;
    }
    
    public void setTrainer(User trainer) {
        this.trainer = trainer;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getSchedule() {
        return schedule;
    }
    
    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }
    
    public AssignmentStatus getStatus() {
        return status;
    }
    
    public void setStatus(AssignmentStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public Set<Equipment> getRequiredEquipment() {
        return requiredEquipment;
    }
    
    public void setRequiredEquipment(Set<Equipment> requiredEquipment) {
        this.requiredEquipment = requiredEquipment;
    }
    
    /**
     * Add equipment to required equipment
     *
     * @param equipment Equipment to add
     */
    public void addEquipment(Equipment equipment) {
        this.requiredEquipment.add(equipment);
    }
    
    /**
     * Remove equipment from required equipment
     *
     * @param equipment Equipment to remove
     */
    public void removeEquipment(Equipment equipment) {
        this.requiredEquipment.remove(equipment);
    }
    
    /**
     * Check if assignment is active (assigned or accepted)
     *
     * @return True if assignment is active
     */
    public boolean isActive() {
        return status == AssignmentStatus.ASSIGNED || status == AssignmentStatus.ACCEPTED;
    }
    
    /**
     * Check if assignment is completed
     *
     * @return True if assignment is completed
     */
    public boolean isCompleted() {
        return status == AssignmentStatus.COMPLETED;
    }
    
    /**
     * Check if assignment is rejected
     *
     * @return True if assignment is rejected
     */
    public boolean isRejected() {
        return status == AssignmentStatus.REJECTED;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Assignment that = (Assignment) o;
        return id != null && Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Assignment{" +
                "id=" + id +
                ", category=" + category +
                ", title='" + title + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
} 