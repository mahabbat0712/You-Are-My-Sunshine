package com.fitnesscenter.controller;

import com.fitnesscenter.model.Order;
import com.fitnesscenter.model.Review;
import com.fitnesscenter.model.TrainingCycle;
import com.fitnesscenter.model.User;
import com.fitnesscenter.service.DiscountService;
import com.fitnesscenter.service.OrderService;
import com.fitnesscenter.service.SessionService;
import com.fitnesscenter.service.TrainingCycleService;
import com.fitnesscenter.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Controller for managing user orders
 */
@Controller
@RequestMapping("/orders")
public class OrderController {

    private static final Logger logger = LogManager.getLogger(OrderController.class);
    
    private final OrderService orderService;
    private final UserService userService;
    private final TrainingCycleService trainingCycleService;
    private final DiscountService discountService;
    private final SessionService sessionService;
    
    @Autowired
    public OrderController(
            OrderService orderService,
            UserService userService,
            TrainingCycleService trainingCycleService,
            DiscountService discountService,
            SessionService sessionService) {
        this.orderService = orderService;
        this.userService = userService;
        this.trainingCycleService = trainingCycleService;
        this.discountService = discountService;
        this.sessionService = sessionService;
        logger.info("OrderController initialized");
    }
    
    /**
     * Get current authenticated user from session
     */
    private User getCurrentUser(HttpServletRequest request) {
        return sessionService.getCurrentUser(request);
    }
    
    /**
     * List all orders for current user
     */
    @GetMapping
    public String listOrders(HttpServletRequest request, Model model) {
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        List<Order> orders;
        
        // Get different orders based on user role
        if (currentUser.hasRole("ADMIN")) {
            orders = orderService.findAllOrders();
            model.addAttribute("isAdmin", true);
        } else if (currentUser.hasRole("TRAINER")) {
            orders = orderService.findOrdersByTrainer(currentUser.getId());
            model.addAttribute("isTrainer", true);
        } else {
            orders = orderService.findOrdersByClient(currentUser.getId());
        }
        
        model.addAttribute("orders", orders);
        return "orders/list";
    }
    
    /**
     * View order details
     */
    @GetMapping("/{id}")
    public String viewOrder(@PathVariable("id") Long id, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        Optional<Order> orderOpt = orderService.findOrderById(id);
        if (!orderOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Order not found");
            return "redirect:/orders";
        }
        
        Order order = orderOpt.get();
        
        // Security check: Only client, assigned trainer or admin can view
        boolean isAuthorized = currentUser.hasRole("ADMIN") ||
                               (order.getClient() != null && order.getClient().getId().equals(currentUser.getId())) ||
                               (order.getTrainer() != null && order.getTrainer().getId().equals(currentUser.getId()));
        
        if (!isAuthorized) {
            redirectAttributes.addFlashAttribute("error", "You are not authorized to view this order");
            return "redirect:/orders";
        }
        
        model.addAttribute("order", order);
        
        // Add trainers list if admin is viewing and order is in PAID status without a trainer
        if (currentUser.hasRole("ADMIN") && 
            order.getStatus() == Order.OrderStatus.PAID && 
            order.getTrainer() == null) {
            model.addAttribute("trainers", userService.findAllTrainers());
        }
        
        // Add review model if client viewing their completed order
        if (currentUser.getId().equals(order.getClient().getId()) && 
                order.getStatus() == Order.OrderStatus.COMPLETED && 
                !order.isReviewed()) {
            model.addAttribute("review", new Review());
        }
        
        return "orders/view";
    }
    
    /**
     * Create new order - step 1: Choose training cycle
     */
    @GetMapping("/create")
    public String createOrderStep1(HttpServletRequest request, Model model) {
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            // Сохраняем намерение пользователя создать заказ
            HttpSession session = request.getSession(true);
            session.setAttribute("intendedDestination", "/orders/create");
            
            return "redirect:/login";
        }
        
        if (!currentUser.hasRole("CLIENT")) {
            model.addAttribute("error", "Только клиенты могут покупать программы");
            return "redirect:/training-cycles";
        }
        
        List<TrainingCycle> trainingCycles = trainingCycleService.findActiveTrainingCycles();
        model.addAttribute("trainingCycles", trainingCycles);
        
        return "orders/create";
    }
    
    /**
     * Direct purchase link from training cycle page
     */
    @GetMapping("/create/preview/{cycleId}")
    public String directPurchasePreview(@PathVariable("cycleId") Long cycleId, 
                                  HttpServletRequest request, 
                                  Model model, 
                                  RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            // Сохраняем ID тренировочного цикла в сессии, чтобы вернуться после входа
            HttpSession session = request.getSession(true);
            session.setAttribute("pendingPurchaseCycleId", cycleId);
            session.setAttribute("intendedDestination", "/orders/create/preview/" + cycleId);
            
            redirectAttributes.addFlashAttribute("info", "Пожалуйста, войдите в систему для покупки программы");
            return "redirect:/login";
        }
        
        if (!currentUser.hasRole("CLIENT")) {
            redirectAttributes.addFlashAttribute("error", "Только клиенты могут покупать программы");
            return "redirect:/training-cycles/" + cycleId;
        }
        
        Optional<TrainingCycle> cycleOpt = trainingCycleService.findTrainingCycleById(cycleId);
        if (!cycleOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Тренировочная программа не найдена");
            return "redirect:/training-cycles";
        }
        
        TrainingCycle cycle = cycleOpt.get();
        if (!cycle.isActive()) {
            redirectAttributes.addFlashAttribute("error", "Эта тренировочная программа недоступна для покупки");
            return "redirect:/training-cycles";
        }
        
        // Create order object
        Order order = new Order(currentUser, cycle);
        
        // Apply discount
        BigDecimal[] discountResult = discountService.applyDiscount(cycle.getPrice(), currentUser.getId());
        order.setDiscountPercentage(discountResult[0]);
        order.setFinalPrice(discountResult[1]);
        
        model.addAttribute("order", order);
        
        return "orders/preview";
    }
    
    /**
     * Create new order - step 2: Order preview
     */
    @GetMapping("/create/preview")
    public String createOrderStep2(@RequestParam("cycleId") Long cycleId, 
                                  HttpServletRequest request, 
                                  Model model, 
                                  RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            // Сохраняем ID тренировочного цикла в сессии, чтобы вернуться после входа
            HttpSession session = request.getSession(true);
            session.setAttribute("pendingPurchaseCycleId", cycleId);
            session.setAttribute("intendedDestination", "/orders/create/preview?cycleId=" + cycleId);
            
            redirectAttributes.addFlashAttribute("info", "Пожалуйста, войдите в систему для покупки программы");
            return "redirect:/login";
        }
        
        if (!currentUser.hasRole("CLIENT")) {
            redirectAttributes.addFlashAttribute("error", "Только клиенты могут покупать программы");
            return "redirect:/training-cycles/" + cycleId;
        }
        
        Optional<TrainingCycle> cycleOpt = trainingCycleService.findTrainingCycleById(cycleId);
        if (!cycleOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Тренировочная программа не найдена");
            return "redirect:/orders/create";
        }
        
        TrainingCycle cycle = cycleOpt.get();
        
        // Create order object
        Order order = new Order(currentUser, cycle);
        
        // Apply discount
        BigDecimal[] discountResult = discountService.applyDiscount(cycle.getPrice(), currentUser.getId());
        order.setDiscountPercentage(discountResult[0]);
        order.setFinalPrice(discountResult[1]);
        
        model.addAttribute("order", order);
        
        return "orders/preview";
    }
    
    /**
     * Create new order - step 3: Confirm and save
     */
    @PostMapping("/create/confirm")
    public String createOrderStep3(@RequestParam("cycleId") Long cycleId, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser(request);
        if (currentUser == null || !currentUser.hasRole("CLIENT")) {
            return "redirect:/login";
        }
        
        try {
            Optional<TrainingCycle> cycleOpt = trainingCycleService.findTrainingCycleById(cycleId);
            if (!cycleOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Training program not found");
                return "redirect:/orders/create";
            }
            
            TrainingCycle cycle = cycleOpt.get();
            
            // Create and save order
            Order order = orderService.createOrder(currentUser.getId(), cycleId);
            
            redirectAttributes.addFlashAttribute("success", "Order created successfully!");
            return "redirect:/orders/" + order.getId();
        } catch (Exception e) {
            logger.error("Error creating order", e);
            redirectAttributes.addFlashAttribute("error", "Failed to create order: " + e.getMessage());
            return "redirect:/orders/create";
        }
    }
    
    /**
     * Cancel an order
     */
    @PostMapping("/{id}/cancel")
    public String cancelOrder(@PathVariable("id") Long id, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        try {
            Optional<Order> orderOpt = orderService.findOrderById(id);
            if (!orderOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Order not found");
                return "redirect:/orders";
            }
            
            Order order = orderOpt.get();
            
            // Security check: Only client or admin can cancel
            boolean isAuthorized = currentUser.hasRole("ADMIN") ||
                                  (order.getClient() != null && order.getClient().getId().equals(currentUser.getId()));
            
            if (!isAuthorized) {
                redirectAttributes.addFlashAttribute("error", "You are not authorized to cancel this order");
                return "redirect:/orders";
            }
            
            // Check if order can be cancelled
            if (!order.canBeCancelled()) {
                redirectAttributes.addFlashAttribute("error", "This order cannot be cancelled");
                return "redirect:/orders/" + id;
            }
            
            boolean cancelled = orderService.cancelOrder(id);
            if (cancelled) {
                redirectAttributes.addFlashAttribute("success", "Order cancelled successfully");
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to cancel order");
            }
            
            return "redirect:/orders";
        } catch (Exception e) {
            logger.error("Error cancelling order", e);
            redirectAttributes.addFlashAttribute("error", "Failed to cancel order: " + e.getMessage());
            return "redirect:/orders/" + id;
        }
    }
    
    /**
     * Submit a review for an order
     */
    @PostMapping("/{id}/review")
    public String submitReview(@PathVariable("id") Long id,
                              @Valid @ModelAttribute("review") Review review,
                              BindingResult bindingResult,
                              HttpServletRequest request,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        try {
            Optional<Order> orderOpt = orderService.findOrderById(id);
            if (!orderOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Order not found");
                return "redirect:/orders";
            }
            
            Order order = orderOpt.get();
            
            // Security check: Only client can review their own order
            boolean isAuthorized = order.getClient() != null && 
                                   order.getClient().getId().equals(currentUser.getId());
            
            if (!isAuthorized) {
                redirectAttributes.addFlashAttribute("error", "You are not authorized to review this order");
                return "redirect:/orders";
            }
            
            // Check if order is completed and not already reviewed
            if (order.getStatus() != Order.OrderStatus.COMPLETED) {
                redirectAttributes.addFlashAttribute("error", "Only completed orders can be reviewed");
                return "redirect:/orders/" + id;
            }
            
            if (order.isReviewed()) {
                redirectAttributes.addFlashAttribute("error", "This order has already been reviewed");
                return "redirect:/orders/" + id;
            }
            
            if (bindingResult.hasErrors()) {
                model.addAttribute("order", order);
                return "orders/view";
            }
            
            // Save the review
            boolean saved = orderService.addReview(id, review);
            if (saved) {
                redirectAttributes.addFlashAttribute("success", "Review submitted successfully");
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to submit review");
            }
            
            return "redirect:/orders/" + id;
        } catch (Exception e) {
            logger.error("Error submitting review", e);
            redirectAttributes.addFlashAttribute("error", "Failed to submit review: " + e.getMessage());
            return "redirect:/orders/" + id;
        }
    }
    
    /**
     * Mark order as paid (admin only)
     */
    @PostMapping("/{id}/mark-paid")
    public String markOrderAsPaid(@PathVariable("id") Long id, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser(request);
        if (currentUser == null || !currentUser.hasRole("ADMIN")) {
            redirectAttributes.addFlashAttribute("error", "Unauthorized");
            return "redirect:/orders";
        }
        
        try {
            boolean updated = orderService.updateOrderStatus(id, Order.OrderStatus.PAID);
            if (updated) {
                redirectAttributes.addFlashAttribute("success", "Order marked as paid successfully");
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to update order status");
            }
            
            return "redirect:/orders/" + id;
        } catch (Exception e) {
            logger.error("Error updating order status", e);
            redirectAttributes.addFlashAttribute("error", "Failed to update order status: " + e.getMessage());
            return "redirect:/orders/" + id;
        }
    }
    
    /**
     * Mark order as completed (admin only)
     */
    @PostMapping("/{id}/mark-completed")
    public String markOrderAsCompleted(@PathVariable("id") Long id, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser(request);
        if (currentUser == null || !currentUser.hasRole("ADMIN")) {
            redirectAttributes.addFlashAttribute("error", "Unauthorized");
            return "redirect:/orders";
        }
        
        try {
            boolean updated = orderService.updateOrderStatus(id, Order.OrderStatus.COMPLETED);
            if (updated) {
                redirectAttributes.addFlashAttribute("success", "Order marked as completed successfully");
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to update order status");
            }
            
            return "redirect:/orders/" + id;
        } catch (Exception e) {
            logger.error("Error updating order status", e);
            redirectAttributes.addFlashAttribute("error", "Failed to update order status: " + e.getMessage());
            return "redirect:/orders/" + id;
        }
    }
    
    /**
     * Assign trainer to order (admin only)
     */
    @PostMapping("/{id}/assign-trainer")
    public String assignTrainer(@PathVariable("id") Long id,
                              @RequestParam("trainerId") Long trainerId,
                              HttpServletRequest request,
                              RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser(request);
        if (currentUser == null || !currentUser.hasRole("ADMIN")) {
            redirectAttributes.addFlashAttribute("error", "Unauthorized");
            return "redirect:/orders";
        }
        
        try {
            boolean assigned = orderService.assignTrainer(id, trainerId);
            if (assigned) {
                redirectAttributes.addFlashAttribute("success", "Trainer assigned successfully");
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to assign trainer");
            }
            
            return "redirect:/orders/" + id;
        } catch (Exception e) {
            logger.error("Error assigning trainer", e);
            redirectAttributes.addFlashAttribute("error", "Failed to assign trainer: " + e.getMessage());
            return "redirect:/orders/" + id;
        }
    }
    
    /**
     * Display payment page for an order
     */
    @GetMapping("/{id}/payment")
    public String showPaymentPage(@PathVariable("id") Long id, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        Optional<Order> orderOpt = orderService.findOrderById(id);
        if (!orderOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Order not found");
            return "redirect:/orders";
        }
        
        Order order = orderOpt.get();
        
        // Security check: Only client who owns the order can pay
        if (order.getClient() == null || !order.getClient().getId().equals(currentUser.getId())) {
            redirectAttributes.addFlashAttribute("error", "You are not authorized to pay for this order");
            return "redirect:/orders";
        }
        
        // Check if order is in pending status
        if (order.getStatus() != Order.OrderStatus.PENDING) {
            redirectAttributes.addFlashAttribute("error", "This order cannot be paid because it's not in pending status");
            return "redirect:/orders/" + id;
        }
        
        model.addAttribute("order", order);
        return "orders/payment";
    }
    
    /**
     * Process payment for an order
     */
    @PostMapping("/process-payment")
    public String processPayment(@RequestParam("orderId") Long orderId, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        try {
            Optional<Order> orderOpt = orderService.findOrderById(orderId);
            if (!orderOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Order not found");
                return "redirect:/orders";
            }
            
            Order order = orderOpt.get();
            
            // Security check: Only client who owns the order can pay
            if (order.getClient() == null || !order.getClient().getId().equals(currentUser.getId())) {
                redirectAttributes.addFlashAttribute("error", "You are not authorized to pay for this order");
                return "redirect:/orders";
            }
            
            // Check if order is in pending status
            if (order.getStatus() != Order.OrderStatus.PENDING) {
                redirectAttributes.addFlashAttribute("error", "This order cannot be paid because it's not in pending status");
                return "redirect:/orders/" + orderId;
            }
            
            // In a real application, payment processing would happen here
            // For demo purposes, mark the order as paid immediately
            boolean updated = orderService.updateOrderStatus(orderId, Order.OrderStatus.PAID);
            
            if (updated) {
                // After successful payment, increment the client's completed cycles count
                // This will be used for applying discounts in future orders
                userService.incrementClientCompletedCycles(currentUser.getId());
                
                redirectAttributes.addFlashAttribute("success", "Payment successful! Your trainer will be assigned shortly.");
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to process payment");
            }
            
            return "redirect:/orders/" + orderId;
            
        } catch (Exception e) {
            logger.error("Error processing payment", e);
            redirectAttributes.addFlashAttribute("error", "Failed to process payment: " + e.getMessage());
            return "redirect:/orders";
        }
    }
} 