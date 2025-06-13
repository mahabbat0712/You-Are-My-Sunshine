package com.fitnesscenter.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Objects;

/**
 * Equipment entity representing equipment types available in the fitness center
 */
public class Equipment {
    private Long id;
    
    @NotBlank(message = "{validation.equipment.name.required}")
    @Size(min = 2, max = 100, message = "{validation.equipment.name.size}")
    private String name;
    
    private String description;
    
    // Constructors
    public Equipment() {
    }
    
    public Equipment(String name) {
        this.name = name;
    }
    
    public Equipment(String name, String description) {
        this.name = name;
        this.description = description;
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
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Equipment equipment = (Equipment) o;
        return id != null && Objects.equals(id, equipment.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Equipment{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
} 