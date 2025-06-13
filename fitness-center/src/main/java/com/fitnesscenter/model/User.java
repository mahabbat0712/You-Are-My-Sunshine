package com.fitnesscenter.model;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/**
 * User entity representing users in the system (clients, trainers, administrators)
 */
public class User {
    private Long id;
    
    @NotBlank(message = "{validation.username.required}")
    @Size(min = 3, max = 50, message = "{validation.username.size}")
    private String username;
    
    @NotBlank(message = "{validation.password.required}")
    private String password;
    
    @NotBlank(message = "{validation.email.required}")
    @Email(message = "{validation.email.format}")
    private String email;
    
    @NotBlank(message = "{validation.firstname.required}")
    @Size(min = 2, max = 50, message = "{validation.name.size}")
    private String firstName;
    
    @NotBlank(message = "{validation.lastname.required}")
    @Size(min = 2, max = 50, message = "{validation.name.size}")
    private String lastName;
    
    private String phone;
    
    private LocalDateTime createdAt;
    
    private boolean active;
    
    private Set<Role> roles = new HashSet<>();
    
    private TrainerProfile trainerProfile;
    
    // Constructors
    public User() {
        this.active = true;
        this.createdAt = LocalDateTime.now();
    }
    
    public User(String username, String email, String firstName, String lastName) {
        this();
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public Set<Role> getRoles() {
        return roles;
    }
    
    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }
    
    /**
     * Add role to user
     *
     * @param role Role to add
     */
    public void addRole(Role role) {
        this.roles.add(role);
    }
    
    /**
     * Remove role from user
     *
     * @param role Role to remove
     */
    public void removeRole(Role role) {
        this.roles.remove(role);
    }
    
    /**
     * Check if user has a specific role
     *
     * @param roleName Name of the role to check
     * @return True if user has the role, false otherwise
     */
    public boolean hasRole(String roleName) {
        return roles.stream().anyMatch(role -> role.getName().equals(roleName));
    }
    
    /**
     * Get full name (first + last name)
     *
     * @return Full name of the user
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    /**
     * Get the trainer profile for this user
     * 
     * @return TrainerProfile or null if user is not a trainer
     */
    public TrainerProfile getTrainerProfile() {
        return trainerProfile;
    }
    
    /**
     * Set the trainer profile for this user
     * 
     * @param trainerProfile The trainer profile to set
     */
    public void setTrainerProfile(TrainerProfile trainerProfile) {
        this.trainerProfile = trainerProfile;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id != null && Objects.equals(id, user.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", active=" + active +
                ", roles=" + roles +
                '}';
    }
} 