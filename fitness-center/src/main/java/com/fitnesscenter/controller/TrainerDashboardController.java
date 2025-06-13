package com.fitnesscenter.controller;

import com.fitnesscenter.model.Assignment;
import com.fitnesscenter.model.Order;
import com.fitnesscenter.model.User;
import com.fitnesscenter.service.AssignmentService;
import com.fitnesscenter.service.OrderService;
import com.fitnesscenter.service.SessionService;
import com.fitnesscenter.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller for trainer dashboard and functionality
 */
@Controller
@RequestMapping("/trainer")
public class TrainerDashboardController {
    
    private static final Logger logger = LogManager.getLogger(TrainerDashboardController.class);
    
    private final UserService userService;
    private final OrderService orderService;
    private final AssignmentService assignmentService;
    private final SessionService sessionService;
    
    @Autowired
    public TrainerDashboardController(
            UserService userService,
            OrderService orderService,
            AssignmentService assignmentService,
            SessionService sessionService) {
        this.userService = userService;
        this.orderService = orderService;
        this.assignmentService = assignmentService;
        this.sessionService = sessionService;
        logger.info("TrainerDashboardController initialized");
    }
    
    /**
     * Get current authenticated user from session
     */
    private User getCurrentUser(HttpServletRequest request) {
        return sessionService.getCurrentUser(request);
    }
    
    /**
     * Trainer dashboard
     */
    @GetMapping("/dashboard")
    public String dashboard(HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        if (!currentUser.hasRole("TRAINER")) {
            redirectAttributes.addFlashAttribute("error", "У вас нет доступа к панели тренера");
            return "redirect:/";
        }
        
        // Получаем заказы, назначенные этому тренеру
        List<Order> assignedOrders = orderService.findOrdersByTrainer(currentUser.getId());
        
        // Фильтруем заказы по статусу
        List<Order> pendingOrders = assignedOrders.stream()
                .filter(order -> order.getStatus() == Order.OrderStatus.PENDING)
                .collect(Collectors.toList());
        
        List<Order> paidOrders = assignedOrders.stream()
                .filter(order -> order.getStatus() == Order.OrderStatus.PAID)
                .collect(Collectors.toList());
        
        List<Order> completedOrders = assignedOrders.stream()
                .filter(order -> order.getStatus() == Order.OrderStatus.COMPLETED)
                .collect(Collectors.toList());
        
        // Получаем назначения тренера
        List<Assignment> assignments = assignmentService.findAssignmentsByTrainer(currentUser.getId());
        
        // Фильтруем назначения по статусу
        List<Assignment> pendingAssignments = assignments.stream()
                .filter(a -> "ASSIGNED".equals(a.getStatus()))
                .collect(Collectors.toList());
        
        List<Assignment> acceptedAssignments = assignments.stream()
                .filter(a -> "ACCEPTED".equals(a.getStatus()))
                .collect(Collectors.toList());
        
        List<Assignment> completedAssignments = assignments.stream()
                .filter(a -> "COMPLETED".equals(a.getStatus()))
                .collect(Collectors.toList());
        
        // Считаем статистику для графика
        Map<String, Integer> assignmentStats = new HashMap<>();
        assignmentStats.put("Назначено", pendingAssignments.size());
        assignmentStats.put("Принято", acceptedAssignments.size());
        assignmentStats.put("Выполнено", completedAssignments.size());
        
        // Передаем данные в представление
        model.addAttribute("pendingOrders", pendingOrders);
        model.addAttribute("paidOrders", paidOrders);
        model.addAttribute("completedOrders", completedOrders);
        model.addAttribute("totalOrders", assignedOrders.size());
        
        model.addAttribute("pendingAssignments", pendingAssignments);
        model.addAttribute("acceptedAssignments", acceptedAssignments);
        model.addAttribute("completedAssignments", completedAssignments);
        model.addAttribute("totalAssignments", assignments.size());
        
        model.addAttribute("assignmentStats", assignmentStats);
        
        return "trainer/dashboard";
    }
    
    /**
     * Trainer's clients list
     */
    @GetMapping("/clients")
    public String clients(HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        if (!currentUser.hasRole("TRAINER")) {
            redirectAttributes.addFlashAttribute("error", "У вас нет доступа к панели тренера");
            return "redirect:/";
        }
        
        // Получаем заказы, назначенные этому тренеру
        List<Order> assignedOrders = orderService.findOrdersByTrainer(currentUser.getId());
        
        // Извлекаем уникальных клиентов из заказов
        List<User> clients = assignedOrders.stream()
                .map(Order::getClient)
                .distinct()
                .collect(Collectors.toList());
        
        model.addAttribute("clients", clients);
        
        return "trainer/clients";
    }
} 