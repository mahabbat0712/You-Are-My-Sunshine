package com.fitnesscenter.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Order entity representing clients purchasing training cycles
 */
public class Order {
    private Long id;
    private User client;
    private TrainingCycle trainingCycle;
    private User trainer;
    private BigDecimal originalPrice;
    private BigDecimal discountPercentage;
    private BigDecimal finalPrice;
    private OrderStatus status;
    private LocalDateTime orderDate;
    private LocalDateTime paymentDate;
    private LocalDateTime completionDate;
    private List<Assignment> assignments = new ArrayList<>();
    private Review review;
    
    /**
     * Order status enum
     */
    public enum OrderStatus {
        PENDING("PENDING"),
        PAID("PAID"),
        COMPLETED("COMPLETED"),
        CANCELLED("CANCELLED");
        
        private final String value;
        
        OrderStatus(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
        
        public static OrderStatus fromValue(String value) {
            for (OrderStatus status : OrderStatus.values()) {
                if (status.value.equals(value)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Invalid order status: " + value);
        }
    }
    
    // Constructors
    public Order() {
        this.orderDate = LocalDateTime.now();
        this.status = OrderStatus.PENDING;
        this.discountPercentage = BigDecimal.ZERO;
    }
    
    public Order(User client, TrainingCycle trainingCycle) {
        this();
        this.client = client;
        this.trainingCycle = trainingCycle;
        this.originalPrice = trainingCycle.getPrice();
        this.finalPrice = trainingCycle.getPrice();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getClient() {
        return client;
    }
    
    public void setClient(User client) {
        this.client = client;
    }
    
    public TrainingCycle getTrainingCycle() {
        return trainingCycle;
    }
    
    public void setTrainingCycle(TrainingCycle trainingCycle) {
        this.trainingCycle = trainingCycle;
    }
    
    public User getTrainer() {
        return trainer;
    }
    
    public void setTrainer(User trainer) {
        this.trainer = trainer;
    }
    
    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }
    
    public void setOriginalPrice(BigDecimal originalPrice) {
        this.originalPrice = originalPrice;
        recalculatePrice();
    }
    
    public BigDecimal getDiscountPercentage() {
        return discountPercentage;
    }
    
    public void setDiscountPercentage(BigDecimal discountPercentage) {
        this.discountPercentage = discountPercentage;
        recalculatePrice();
    }
    
    public BigDecimal getFinalPrice() {
        return finalPrice;
    }
    
    public void setFinalPrice(BigDecimal finalPrice) {
        this.finalPrice = finalPrice;
    }
    
    public OrderStatus getStatus() {
        return status;
    }
    
    public void setStatus(OrderStatus status) {
        this.status = status;
        
        // Update payment or completion date when status changes
        if (status == OrderStatus.PAID && paymentDate == null) {
            this.paymentDate = LocalDateTime.now();
        } else if (status == OrderStatus.COMPLETED && completionDate == null) {
            this.completionDate = LocalDateTime.now();
        }
    }
    
    public LocalDateTime getOrderDate() {
        return orderDate;
    }
    
    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }
    
    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }
    
    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }
    
    public LocalDateTime getCompletionDate() {
        return completionDate;
    }
    
    public void setCompletionDate(LocalDateTime completionDate) {
        this.completionDate = completionDate;
    }
    
    public List<Assignment> getAssignments() {
        return assignments;
    }
    
    public void setAssignments(List<Assignment> assignments) {
        this.assignments = assignments;
    }
    
    public Review getReview() {
        return review;
    }
    
    public void setReview(Review review) {
        this.review = review;
    }
    
    /**
     * Add assignment to order
     *
     * @param assignment Assignment to add
     */
    public void addAssignment(Assignment assignment) {
        this.assignments.add(assignment);
    }
    
    /**
     * Recalculate final price based on original price and discount
     */
    private void recalculatePrice() {
        if (originalPrice != null && discountPercentage != null) {
            BigDecimal discount = originalPrice.multiply(discountPercentage)
                    .divide(new BigDecimal(100));
            this.finalPrice = originalPrice.subtract(discount);
        }
    }
    
    /**
     * Check if the order has been paid
     *
     * @return True if order status is PAID or COMPLETED
     */
    public boolean isPaid() {
        return status == OrderStatus.PAID || status == OrderStatus.COMPLETED;
    }
    
    /**
     * Check if the order has been reviewed
     *
     * @return True if the order has a review
     */
    public boolean isReviewed() {
        return review != null;
    }
    
    /**
     * Check if the order can be cancelled
     *
     * @return True if the order can be cancelled
     */
    public boolean canBeCancelled() {
        return status == OrderStatus.PENDING;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return id != null && Objects.equals(id, order.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", client=" + (client != null ? client.getUsername() : "null") +
                ", trainingCycle=" + (trainingCycle != null ? trainingCycle.getName() : "null") +
                ", status=" + status +
                ", orderDate=" + orderDate +
                '}';
    }
} 