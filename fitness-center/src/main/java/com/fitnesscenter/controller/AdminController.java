package com.fitnesscenter.controller;

import com.fitnesscenter.model.TrainingCycle;
import com.fitnesscenter.model.User;
import com.fitnesscenter.model.Role;
import com.fitnesscenter.service.OrderService;
import com.fitnesscenter.service.SessionService;
import com.fitnesscenter.service.TrainingCycleService;
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

/**
 * Controller for admin dashboard and management functions
 */
@Controller
@RequestMapping("/admin")
public class AdminController {
    
    private static final Logger logger = LogManager.getLogger(AdminController.class);
    
    private final UserService userService;
    private final OrderService orderService;
    private final TrainingCycleService trainingCycleService;
    private final SessionService sessionService;
    
    @Autowired
    public AdminController(
            UserService userService,
            OrderService orderService,
            TrainingCycleService trainingCycleService,
            SessionService sessionService) {
        this.userService = userService;
        this.orderService = orderService;
        this.trainingCycleService = trainingCycleService;
        this.sessionService = sessionService;
        logger.info("AdminController initialized");
    }
    
    /**
     * Get current authenticated user from session
     */
    private User getCurrentUser(HttpServletRequest request) {
        return sessionService.getCurrentUser(request);
    }
    
    /**
     * Admin dashboard
     */
    @GetMapping("/dashboard")
    public String dashboard(HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        if (!currentUser.hasRole("ADMIN")) {
            redirectAttributes.addFlashAttribute("error", "У вас нет доступа к панели администратора");
            return "redirect:/";
        }
        
        // Собираем статистику для дашборда
        long totalUsers = userService.countUsers();
        long totalClients = userService.countUsersByRole("CLIENT");
        long totalTrainers = userService.countUsersByRole("TRAINER");
        long totalOrders = orderService.countOrders();
        List<TrainingCycle> activeCycles = trainingCycleService.findActiveTrainingCycles();
        
        // Данные для графика заказов (заглушка, в реальном приложении данные будут из БД)
        Map<String, Long> orderStats = new HashMap<>();
        orderStats.put("PENDING", 0L);
        orderStats.put("PAID", 0L);
        orderStats.put("COMPLETED", 0L);
        orderStats.put("CANCELLED", 0L);
        
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalClients", totalClients);
        model.addAttribute("totalTrainers", totalTrainers);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("activeCycles", activeCycles.size());
        model.addAttribute("orderStats", orderStats);
        
        return "admin/dashboard";
    }
    
    /**
     * Users management page
     */
    @GetMapping("/users")
    public String showAllUsers(HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        if (!currentUser.hasRole("ADMIN")) {
            redirectAttributes.addFlashAttribute("error", "У вас нет доступа к управлению пользователями");
            return "redirect:/";
        }
        
        List<User> users = userService.findAllUsers();
        model.addAttribute("users", users);
        
        return "admin/users/list";
    }
    
    /**
     * Training cycles management page - redirects to the dedicated controller
     */
    @GetMapping("/training-cycles-redirect")
    public String trainingCycles(HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        if (!currentUser.hasRole("ADMIN")) {
            redirectAttributes.addFlashAttribute("error", "У вас нет доступа к управлению тренировочными циклами");
            return "redirect:/";
        }
        
        // Перенаправляем на новый контроллер
        return "redirect:/admin/training-cycles";
    }
    
    /**
     * Orders management page
     */
    @GetMapping("/orders")
    public String orders(HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        if (!currentUser.hasRole("ADMIN")) {
            redirectAttributes.addFlashAttribute("error", "У вас нет доступа к управлению заказами");
            return "redirect:/";
        }
        
        // Получаем статистику заказов
        List<Object[]> orderStats = orderService.getOrderStatistics();
        
        // Подсчитываем общее количество заказов и заказы в различных статусах
        long totalOrders = orderService.countOrders();
        
        // В реальном приложении эти значения будут получены из базы данных
        // через соответствующие методы сервиса
        long completedOrders = 0;
        long paidOrders = 0;
        long pendingOrders = 0;
        
        // Вычисляем значения из полученной статистики
        for (Object[] stat : orderStats) {
            String status = (String) stat[0];
            Long count = (Long) stat[1];
            
            if ("COMPLETED".equals(status)) {
                completedOrders = count;
            } else if ("PAID".equals(status)) {
                paidOrders = count;
            } else if ("PENDING".equals(status)) {
                pendingOrders = count;
            }
        }
        
        model.addAttribute("orderStats", orderStats);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("completedOrders", completedOrders);
        model.addAttribute("paidOrders", paidOrders);
        model.addAttribute("pendingOrders", pendingOrders);
        
        return "admin/orders/stats";
    }
    
    /**
     * Initialize admin and trainer accounts
     */
    @GetMapping("/init-accounts")
    public String initializeAccounts(RedirectAttributes redirectAttributes) {
        try {
            logger.info("Starting initialization of accounts");
            // Проверяем, существует ли уже администратор
            if (userService.findByUsername("admin").isPresent()) {
                logger.info("Administrator already exists");
                redirectAttributes.addFlashAttribute("warning", "Администратор уже существует в системе");
                return "redirect:/login";
            }
            
            // Создаем администратора
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setEmail("test@example.com");
            adminUser.setFirstName("Администратор");
            adminUser.setLastName("Системы");
            adminUser.setActive(true);
            
            logger.info("Creating administrator user");
            User createdAdmin = userService.createUser(adminUser, "admin123");
            userService.assignRole(createdAdmin.getId(), "ADMIN");
            logger.info("Admin created successfully: {}", createdAdmin.getUsername());
            
            // Создаем тренера
            User trainerUser = new User();
            trainerUser.setUsername("trainer");
            trainerUser.setEmail("trainer123@example.com");
            trainerUser.setFirstName("Тренер");
            trainerUser.setLastName("Иванов");
            trainerUser.setActive(true);
            
            logger.info("Creating trainer user");
            User createdTrainer = userService.createUser(trainerUser, "trainer123");
            userService.assignRole(createdTrainer.getId(), "TRAINER");
            logger.info("Trainer created successfully: {}", createdTrainer.getUsername());
            
            redirectAttributes.addFlashAttribute("success", 
                    "Аккаунты успешно созданы! Администратор: admin/admin123, Тренер: trainer/trainer123");
            
            return "redirect:/login";
        } catch (Exception e) {
            logger.error("Ошибка при создании аккаунтов", e);
            redirectAttributes.addFlashAttribute("error", "Ошибка при создании аккаунтов: " + e.getMessage());
            return "redirect:/login";
        }
    }
    
    /**
     * Check if roles exist
     */
    @GetMapping("/check-roles")
    public String checkRoles(Model model) {
        try {
            // Здесь мы просто проверим, есть ли роли в системе
            boolean adminRoleExists = userService.roleExists("ADMIN");
            boolean trainerRoleExists = userService.roleExists("TRAINER");
            boolean clientRoleExists = userService.roleExists("CLIENT");
            
            model.addAttribute("adminRoleExists", adminRoleExists);
            model.addAttribute("trainerRoleExists", trainerRoleExists);
            model.addAttribute("clientRoleExists", clientRoleExists);
            
            // Также проверим наличие пользователей с этими ролями
            long adminCount = userService.countUsersByRole("ADMIN");
            long trainerCount = userService.countUsersByRole("TRAINER");
            long clientCount = userService.countUsersByRole("CLIENT");
            
            model.addAttribute("adminCount", adminCount);
            model.addAttribute("trainerCount", trainerCount);
            model.addAttribute("clientCount", clientCount);
            
            return "admin/check-roles";
        } catch (Exception e) {
            logger.error("Ошибка при проверке ролей", e);
            model.addAttribute("error", "Ошибка при проверке ролей: " + e.getMessage());
            return "admin/check-roles";
        }
    }
} 