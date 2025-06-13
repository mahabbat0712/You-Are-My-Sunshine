package com.fitnesscenter.service.impl;

import com.fitnesscenter.dao.OrderDao;
import com.fitnesscenter.dao.TrainingCycleDao;
import com.fitnesscenter.dao.UserDao;
import com.fitnesscenter.model.Order;
import com.fitnesscenter.model.Review;
import com.fitnesscenter.model.TrainingCycle;
import com.fitnesscenter.model.User;
import com.fitnesscenter.service.DiscountService;
import com.fitnesscenter.service.OrderService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of OrderService
 */
@Service
public class OrderServiceImpl implements OrderService {
    private static final Logger logger = LogManager.getLogger(OrderServiceImpl.class);
    
    private final OrderDao orderDao;
    private final UserDao userDao;
    private final TrainingCycleDao trainingCycleDao;
    private final DiscountService discountService;
    
    @Autowired
    public OrderServiceImpl(OrderDao orderDao, UserDao userDao, 
                           TrainingCycleDao trainingCycleDao, 
                           DiscountService discountService) {
        this.orderDao = orderDao;
        this.userDao = userDao;
        this.trainingCycleDao = trainingCycleDao;
        this.discountService = discountService;
        logger.info("OrderServiceImpl initialized");
    }

    @Override
    public Optional<Order> findById(Long id) {
        try {
            return orderDao.findById(id);
        } catch (Exception e) {
            logger.error("Error finding order by id: {}", id, e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<Order> findOrderById(Long id) {
        return findById(id);
    }

    @Override
    public List<Order> findByClientId(Long clientId) {
        try {
            return orderDao.findByClientId(clientId);
        } catch (Exception e) {
            logger.error("Error finding orders for client: {}", clientId, e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<Order> findOrdersByClient(Long clientId) {
        return findByClientId(clientId);
    }

    @Override
    public List<Order> findByTrainerId(Long trainerId) {
        try {
            return orderDao.findByTrainerId(trainerId);
        } catch (Exception e) {
            logger.error("Error finding orders for trainer: {}", trainerId, e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<Order> findOrdersByTrainer(Long trainerId) {
        return findByTrainerId(trainerId);
    }

    @Override
    public List<Order> findOrdersAssignedToTrainer(Long trainerId) {
        try {
            List<Order> trainerOrders = orderDao.findByTrainerId(trainerId);
            // We're assuming "assigned" means the order has a status other than PENDING or CANCELLED
            return trainerOrders.stream()
                .filter(order -> order.getStatus() != Order.OrderStatus.PENDING && 
                                order.getStatus() != Order.OrderStatus.CANCELLED)
                .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error finding assigned orders for trainer: {}", trainerId, e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<Order> findAllOrders() {
        try {
            return orderDao.findAll();
        } catch (Exception e) {
            logger.error("Error finding all orders", e);
            return Collections.emptyList();
        }
    }

    @Override
    public Order createOrder(Order order) {
        try {
            return orderDao.save(order);
        } catch (Exception e) {
            logger.error("Error creating order", e);
            throw new RuntimeException("Failed to create order", e);
        }
    }

    @Override
    public Order createOrder(Long clientId, Long cycleId) {
        try {
            Optional<User> clientOpt = userDao.findById(clientId);
            Optional<TrainingCycle> cycleOpt = trainingCycleDao.findById(cycleId);
            
            if (!clientOpt.isPresent()) {
                throw new IllegalArgumentException("Client not found with ID: " + clientId);
            }
            
            if (!cycleOpt.isPresent()) {
                throw new IllegalArgumentException("Training cycle not found with ID: " + cycleId);
            }
            
            User client = clientOpt.get();
            TrainingCycle cycle = cycleOpt.get();
            
            // Since TrainingCycle doesn't have a getTrainer method, we'll set trainer to null initially
            // The trainer can be assigned later with assignTrainer method
            User trainer = null;
            
            Order order = new Order();
            order.setClient(client);
            order.setTrainer(trainer);
            order.setTrainingCycle(cycle);
            order.setOriginalPrice(cycle.getPrice());
            
            BigDecimal finalPrice = calculateFinalPrice(cycle.getPrice(), client);
            order.setFinalPrice(finalPrice);
            
            // Calculate discount percentage
            if (!cycle.getPrice().equals(BigDecimal.ZERO)) {
                BigDecimal discountAmount = cycle.getPrice().subtract(finalPrice);
                BigDecimal percentage = discountAmount.multiply(BigDecimal.valueOf(100))
                        .divide(cycle.getPrice(), 2, BigDecimal.ROUND_HALF_UP);
                order.setDiscountPercentage(percentage);
            }
            
            return orderDao.save(order);
        } catch (Exception e) {
            logger.error("Error creating order for client {} and cycle {}", clientId, cycleId, e);
            throw new RuntimeException("Failed to create order", e);
        }
    }

    @Override
    public Order updateOrder(Order order) {
        try {
            return orderDao.update(order);
        } catch (Exception e) {
            logger.error("Error updating order: {}", order.getId(), e);
            throw new RuntimeException("Failed to update order", e);
        }
    }

    @Override
    public boolean deleteOrder(Long id) {
        try {
            return orderDao.delete(id);
        } catch (Exception e) {
            logger.error("Error deleting order: {}", id, e);
            return false;
        }
    }

    @Override
    public boolean cancelOrder(Long id) {
        try {
            Optional<Order> orderOpt = findById(id);
            if (!orderOpt.isPresent()) {
                return false;
            }
            
            Order order = orderOpt.get();
            if (!order.canBeCancelled()) {
                return false;
            }
            
            order.setStatus(Order.OrderStatus.CANCELLED);
            orderDao.update(order);
            return true;
        } catch (Exception e) {
            logger.error("Error cancelling order: {}", id, e);
            return false;
        }
    }

    @Override
    public boolean changeStatus(Long orderId, String status) {
        try {
            Order.OrderStatus orderStatus = Order.OrderStatus.fromValue(status);
            return updateOrderStatus(orderId, orderStatus);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid order status: {}", status, e);
            return false;
        } catch (Exception e) {
            logger.error("Error changing status for order: {}", orderId, e);
            return false;
        }
    }

    @Override
    public boolean updateOrderStatus(Long orderId, Order.OrderStatus status) {
        try {
            Optional<Order> orderOpt = findById(orderId);
            if (!orderOpt.isPresent()) {
                return false;
            }
            
            Order order = orderOpt.get();
            order.setStatus(status);
            
            // Update relevant dates based on status
            if (status == Order.OrderStatus.PAID) {
                order.setPaymentDate(LocalDateTime.now());
            } else if (status == Order.OrderStatus.COMPLETED) {
                order.setCompletionDate(LocalDateTime.now());
            }
            
            orderDao.update(order);
            return true;
        } catch (Exception e) {
            logger.error("Error updating status for order: {}", orderId, e);
            return false;
        }
    }

    @Override
    public BigDecimal calculateFinalPrice(BigDecimal originalPrice, User client) {
        try {
            BigDecimal discount = discountService.calculateDiscountForClient(client.getId());
            if (discount == null || discount.equals(BigDecimal.ZERO)) {
                return originalPrice;
            }
            
            // Calculate discount amount based on percentage
            BigDecimal discountAmount = originalPrice.multiply(discount)
                .divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
                
            return originalPrice.subtract(discountAmount);
        } catch (Exception e) {
            logger.error("Error calculating final price", e);
            return originalPrice; // Return original price if calculation fails
        }
    }

    @Override
    public List<Order> findAll(int page, int size) {
        try {
            return orderDao.findAll(page, size);
        } catch (Exception e) {
            logger.error("Error finding orders with pagination", e);
            return Collections.emptyList();
        }
    }

    @Override
    public long countOrders() {
        try {
            return orderDao.count();
        } catch (Exception e) {
            logger.error("Error counting orders", e);
            return 0;
        }
    }
    
    /**
     * Add a review to an order
     *
     * @param orderId Order ID
     * @param review Review to add
     * @return true if review added successfully, false otherwise
     */
    @Override
    public boolean addReview(Long orderId, Review review) {
        try {
            Optional<Order> orderOpt = findById(orderId);
            if (!orderOpt.isPresent()) {
                return false;
            }
            
            Order order = orderOpt.get();
            order.setReview(review);
            orderDao.update(order);
            return true;
        } catch (Exception e) {
            logger.error("Error adding review to order: {}", orderId, e);
            return false;
        }
    }
    
    /**
     * Assign a trainer to an order
     *
     * @param orderId Order ID
     * @param trainerId Trainer ID
     * @return true if trainer assigned successfully, false otherwise
     */
    @Override
    public boolean assignTrainer(Long orderId, Long trainerId) {
        try {
            Optional<Order> orderOpt = findById(orderId);
            if (!orderOpt.isPresent()) {
                logger.error("Order not found with ID: {}", orderId);
                return false;
            }
            
            Optional<User> trainerOpt = userDao.findById(trainerId);
            if (!trainerOpt.isPresent()) {
                logger.error("Trainer not found with ID: {}", trainerId);
                return false;
            }
            
            // Verify that the user is a trainer
            User trainer = trainerOpt.get();
            if (!trainer.hasRole("TRAINER")) {
                logger.error("User with ID {} is not a trainer", trainerId);
                return false;
            }
            
            Order order = orderOpt.get();
            order.setTrainer(trainer);
            orderDao.update(order);
            
            logger.info("Trainer {} assigned to order {}", trainerId, orderId);
            return true;
        } catch (Exception e) {
            logger.error("Error assigning trainer to order: {}", orderId, e);
            return false;
        }
    }

    @Override
    public List<Object[]> getOrderStatistics() {
        try {
            // В реальном приложении это был бы специальный метод DAO, 
            // который выполняет сложный SQL-запрос для аналитики
            // Сейчас используем заглушку с примерными данными
            List<Object[]> result = new ArrayList<>();
            result.add(new Object[]{"PENDING", 5L});
            result.add(new Object[]{"PAID", 10L});
            result.add(new Object[]{"COMPLETED", 15L});
            result.add(new Object[]{"CANCELLED", 3L});
            return result;
        } catch (Exception e) {
            logger.error("Error getting order statistics", e);
            return Collections.emptyList();
        }
    }
} 