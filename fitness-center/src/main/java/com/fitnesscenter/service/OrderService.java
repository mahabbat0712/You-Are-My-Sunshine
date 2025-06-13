package com.fitnesscenter.service;

import com.fitnesscenter.model.Order;
import com.fitnesscenter.model.Review;
import com.fitnesscenter.model.User;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing orders
 */
public interface OrderService {
    
    /**
     * Find order by ID
     *
     * @param id Order ID
     * @return Optional with order if found, empty optional otherwise
     */
    Optional<Order> findById(Long id);
    
    /**
     * Find order by ID (alias for findById)
     *
     * @param id Order ID
     * @return Optional with order if found, empty optional otherwise
     */
    Optional<Order> findOrderById(Long id);
    
    /**
     * Find all orders for a client
     *
     * @param clientId Client ID
     * @return List of client's orders
     */
    List<Order> findByClientId(Long clientId);
    
    /**
     * Find all orders for a client (alias for findByClientId)
     *
     * @param clientId Client ID
     * @return List of client's orders
     */
    List<Order> findOrdersByClient(Long clientId);
    
    /**
     * Find all orders for a trainer
     *
     * @param trainerId Trainer ID
     * @return List of trainer's orders
     */
    List<Order> findByTrainerId(Long trainerId);
    
    /**
     * Find all orders for a trainer (alias for findByTrainerId)
     *
     * @param trainerId Trainer ID
     * @return List of trainer's orders
     */
    List<Order> findOrdersByTrainer(Long trainerId);
    
    /**
     * Find all orders assigned to a trainer
     *
     * @param trainerId Trainer ID
     * @return List of orders assigned to the trainer
     */
    List<Order> findOrdersAssignedToTrainer(Long trainerId);
    
    /**
     * Find all orders
     *
     * @return List of all orders
     */
    List<Order> findAllOrders();
    
    /**
     * Create a new order
     *
     * @param order Order to create
     * @return Created order with ID
     */
    Order createOrder(Order order);
    
    /**
     * Create a new order with client and training cycle IDs
     *
     * @param clientId Client ID
     * @param cycleId Training cycle ID
     * @return Created order with ID
     */
    Order createOrder(Long clientId, Long cycleId);
    
    /**
     * Update an existing order
     *
     * @param order Order to update
     * @return Updated order
     */
    Order updateOrder(Order order);
    
    /**
     * Delete order by ID
     *
     * @param id Order ID
     * @return true if deleted successfully, false otherwise
     */
    boolean deleteOrder(Long id);
    
    /**
     * Cancel an order
     *
     * @param id Order ID
     * @return true if cancelled successfully, false otherwise
     */
    boolean cancelOrder(Long id);
    
    /**
     * Change order status
     *
     * @param orderId Order ID
     * @param status New status
     * @return true if status changed successfully, false otherwise
     */
    boolean changeStatus(Long orderId, String status);
    
    /**
     * Update order status
     *
     * @param orderId Order ID
     * @param status New status
     * @return true if status updated successfully, false otherwise
     */
    boolean updateOrderStatus(Long orderId, Order.OrderStatus status);
    
    /**
     * Calculate final price with applicable discounts
     *
     * @param originalPrice Original price
     * @param client Client user
     * @return Final price after discounts
     */
    BigDecimal calculateFinalPrice(BigDecimal originalPrice, User client);
    
    /**
     * Find all orders (with pagination)
     *
     * @param page Page number (1-based)
     * @param size Page size
     * @return List of orders on the specified page
     */
    List<Order> findAll(int page, int size);
    
    /**
     * Count total number of orders
     *
     * @return Total number of orders
     */
    long countOrders();
    
    /**
     * Add a review to an order
     *
     * @param orderId Order ID
     * @param review Review to add
     * @return true if review added successfully, false otherwise
     */
    boolean addReview(Long orderId, Review review);
    
    /**
     * Assign a trainer to an order
     *
     * @param orderId Order ID
     * @param trainerId Trainer ID
     * @return true if trainer assigned successfully, false otherwise
     */
    boolean assignTrainer(Long orderId, Long trainerId);
    
    /**
     * Get order statistics for admin dashboard
     *
     * @return List of object arrays containing [status, count]
     */
    List<Object[]> getOrderStatistics();
} 