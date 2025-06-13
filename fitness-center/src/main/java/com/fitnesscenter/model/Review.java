package com.fitnesscenter.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Review entity representing client reviews for completed training cycles
 */
public class Review {
    private Long id;
    private Order order;
    private int rating; // 1-5
    private String comment;
    private LocalDateTime createdAt;
    
    public Review() {
        this.createdAt = LocalDateTime.now();
    }
    
    public Review(Order order, int rating, String comment) {
        this();
        this.order = order;
        this.rating = rating;
        this.comment = comment;
    }
    
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
    
    public int getRating() {
        return rating;
    }
    
    public void setRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
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
                ", order=" + (order != null ? order.getId() : null) +
                ", rating=" + rating +
                ", comment='" + (comment != null ? comment.substring(0, Math.min(20, comment.length())) + "..." : null) + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
} 