package com.fitnesscenter.model;

import java.util.Objects;

/**
 * Role entity representing user roles in the system
 */
public class Role {
    private Integer id;
    private String name;
    
    // Predefined roles
    public static final String ROLE_CLIENT = "CLIENT";
    public static final String ROLE_TRAINER = "TRAINER";
    public static final String ROLE_ADMIN = "ADMIN";
    
    // Constructors
    public Role() {
    }
    
    public Role(Integer id, String name) {
        this.id = id;
        this.name = name;
    }
    
    public Role(String name) {
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
        Role role = (Role) o;
        return Objects.equals(name, role.name);
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