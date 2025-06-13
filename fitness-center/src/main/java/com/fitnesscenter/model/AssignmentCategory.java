package com.fitnesscenter.model;

import java.util.Objects;

/**
 * AssignmentCategory entity representing categories of assignments
 */
public class AssignmentCategory {
    private Integer id;
    private String name;
    
    // Predefined categories
    public static final String CATEGORY_EXERCISE = "EXERCISE";
    public static final String CATEGORY_EQUIPMENT = "EQUIPMENT";
    public static final String CATEGORY_DIET = "DIET";
    
    // Constructors
    public AssignmentCategory() {
    }
    
    public AssignmentCategory(Integer id, String name) {
        this.id = id;
        this.name = name;
    }
    
    public AssignmentCategory(String name) {
        this.name = name;
    }
    
    // Getters and Setters
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
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssignmentCategory that = (AssignmentCategory) o;
        return Objects.equals(name, that.name);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
    
    @Override
    public String toString() {
        return name;
    }
} 