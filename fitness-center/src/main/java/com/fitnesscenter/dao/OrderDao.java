package com.fitnesscenter.dao;

import com.fitnesscenter.model.Order;

import java.util.List;
import java.util.Optional;

/**
 * Data access object interface for Order entities
 */
public interface OrderDao {
    
    /**
     * Find order by ID
     *
     * @param id Order ID
     * @return Optional with order if found, empty optional otherwise
     */
    Optional<Order> findById(Long id);
    
    /**
     * Find all orders
     *
     * @return List of all orders
     */
    List<Order> findAll();
    
    /**
     * Find orders with pagination
     *
     * @param page Page number (1-based)
     * @param size Page size
     * @return List of orders on the specified page
     */
    List<Order> findAll(int page, int size);
    
    /**
     * Find orders by client ID
     *
     * @param clientId Client ID
     * @return List of orders for the specified client
     */
    List<Order> findByClientId(Long clientId);
    
    /**
     * Find orders by trainer ID
     *
     * @param trainerId Trainer ID
     * @return List of orders for the specified trainer
     */
    List<Order> findByTrainerId(Long trainerId);
    
    /**
     * Save a new order
     *
     * @param order Order to save
     * @return Saved order with generated ID
     */
    Order save(Order order);
    
    /**
     * Update an existing order
     *
     * @param order Order to update
     * @return Updated order
     */
    Order update(Order order);
    
    /**
     * Delete an order
     *
     * @param id Order ID
     * @return true if deleted successfully, false otherwise
     */
    boolean delete(Long id);
    
    /**
     * Count total number of orders
     *
     * @return Total count of orders
     */
    long count();
} 