package com.fitnesscenter.model;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Review entity representing client reviews for completed training cycles
 */
public class Review {
    private Long id;
    
    @NotNull(message = "{validation.review.order.required}")
    private Order order;
    
    @NotNull(message = "{validation.review.rating.required}")
    @Min(value = 1, message = "{validation.review.rating.min}")
    @Max(value = 5, message = "{validation.review.rating.max}")
    private Integer rating;
    
    private String comment;
    
    private LocalDateTime createdAt;
    
    // Constructors
    public Review() {
        this.createdAt = LocalDateTime.now();
    }
    
    public Review(Order order, Integer rating) {
        this();
        this.order = order;
        this.rating = rating;
    }
    
    public Review(Order order, Integer rating, String comment) {
        this(order, rating);
        this.comment = comment;
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
    
    public Integer getRating() {
        return rating;
    }
    
    public void setRating(Integer rating) {
        this.rating = rating;
    }
    
    public String getComment() {
        return comment;
    }
    
    public void setComment(String comment) {
        this.comment = comment;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    /**
     * Check if review has a comment
     *
     * @return True if comment is not null or empty
     */
    public boolean hasComment() {
        return comment != null && !comment.trim().isEmpty();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Review review = (Review) o;
        return id != null && Objects.equals(id, review.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Review{" +
                "id=" + id +
                ", order=" + order.getId() +
                ", rating=" + rating +
                ", createdAt=" + createdAt +
                '}';
    }
} 