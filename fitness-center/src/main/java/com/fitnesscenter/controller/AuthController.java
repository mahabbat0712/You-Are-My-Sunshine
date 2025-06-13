package com.fitnesscenter.controller;

import com.fitnesscenter.model.Role;
import com.fitnesscenter.model.User;
import com.fitnesscenter.service.SessionService;
import com.fitnesscenter.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.Optional;

/**
 * Controller for authentication and user registration
 */
@Controller
@RequestMapping("/")
public class AuthController {
    private static final Logger logger = LogManager.getLogger(AuthController.class);
    
    private final UserService userService;
    private final SessionService sessionService;
    
    @Autowired
    public AuthController(UserService userService, SessionService sessionService) {
        this.userService = userService;
        this.sessionService = sessionService;
    }
    
    /**
     * Show login page
     *
     * @param error Error flag
     * @param logout Logout flag
     * @param model Model
     * @return View name
     */
    @GetMapping("/login")
    public String login(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            HttpServletRequest request,
            Model model) {
        
        // Проверяем, авторизован ли пользователь
        User currentUser = sessionService.getCurrentUser(request);
        if (currentUser != null) {
            // Пользователь уже авторизован, перенаправляем в зависимости от роли
            logger.info("Already authenticated user {} tried to access login page, redirecting", currentUser.getUsername());
            
            if (currentUser.hasRole("ADMIN")) {
                return "redirect:/admin/dashboard";
            } else if (currentUser.hasRole("TRAINER")) {
                return "redirect:/trainer/dashboard";
            } else {
                return "redirect:/profile";
            }
        }
        
        if (error != null) {
            model.addAttribute("errorMessage", "Invalid username or password");
        }
        
        if (logout != null) {
            model.addAttribute("successMessage", "You have been logged out successfully");
        }
        
        logger.info("Showing login page");
        return "auth/login";
    }
    
    /**
     * Process login
     *
     * @param username Username
     * @param password Password
     * @param request HTTP request
     * @param redirectAttributes Redirect attributes
     * @return Redirect URL
     */
    @PostMapping("/login")
    public String processLogin(
            @RequestParam String username,
            @RequestParam String password,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        
        try {
            Optional<User> userOpt = userService.authenticate(username, password);
            
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                
                // Создаем сессию и устанавливаем более длительный тайм-аут (8 часов)
                HttpSession session = sessionService.createSession(request, user);

                // Проверяем, есть ли намерение перейти на определенную страницу
                String intendedDestination = (String) session.getAttribute("intendedDestination");
                if (intendedDestination != null) {
                    // Удаляем атрибут из сессии
                    session.removeAttribute("intendedDestination");
                    
                    // Перенаправляем на запланированную страницу
                    return "redirect:" + intendedDestination;
                }

                // Проверяем, есть ли отложенная покупка
                Long pendingPurchaseCycleId = (Long) session.getAttribute("pendingPurchaseCycleId");
                if (pendingPurchaseCycleId != null) {
                    // Удаляем атрибут из сессии
                    session.removeAttribute("pendingPurchaseCycleId");
                    
                    // Если пользователь клиент, перенаправляем его к покупке
                    if (user.hasRole("CLIENT")) {
                        return "redirect:/orders/create/preview/" + pendingPurchaseCycleId;
                    }
                }
                
                // В зависимости от роли пользователя перенаправляем на соответствующую страницу
                if (user.hasRole("ADMIN")) {
                    return "redirect:/admin/dashboard";
                } else if (user.hasRole("TRAINER")) {
                    return "redirect:/trainer/dashboard";
                } else {
                    return "redirect:/profile";
                }
            } else {
                redirectAttributes.addFlashAttribute("error", "Неверное имя пользователя или пароль");
                return "redirect:/login";
            }
        } catch (Exception e) {
            logger.error("Ошибка аутентификации", e);
            redirectAttributes.addFlashAttribute("error", "Произошла ошибка при входе в систему. Пожалуйста, попробуйте снова.");
            return "redirect:/login";
        }
    }
    
    /**
     * Process logout - GET method
     *
     * @param request HTTP request
     * @return Redirect URL
     */
    @GetMapping("/logout")
    public String logoutGet(HttpServletRequest request) {
        // Use session service to invalidate session
        sessionService.invalidateSession(request);
        return "redirect:/login?logout=true";
    }
    
    /**
     * Process logout - POST method
     *
     * @param request HTTP request
     * @return Redirect URL
     */
    @PostMapping("/logout")
    public String logoutPost(HttpServletRequest request) {
        // Use session service to invalidate session
        sessionService.invalidateSession(request);
        return "redirect:/login?logout=true";
    }
    
    /**
     * Show registration form
     *
     * @param model Model
     * @return View name
     */
    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }
    
    /**
     * Process registration
     *
     * @param user User to register
     * @param bindingResult Validation result
     * @param password Raw password
     * @param redirectAttributes Redirect attributes
     * @return Redirect URL
     */
    @PostMapping("/register")
    public String processRegister(
            @Valid @ModelAttribute("user") User user,
            BindingResult bindingResult,
            @RequestParam String password,
            RedirectAttributes redirectAttributes) {
        
        // Check validation errors
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }
        
        // Check if username or email already exists
        if (userService.usernameExists(user.getUsername())) {
            bindingResult.rejectValue("username", "error.user", "Username already exists");
            return "auth/register";
        }
        
        if (userService.emailExists(user.getEmail())) {
            bindingResult.rejectValue("email", "error.user", "Email already exists");
            return "auth/register";
        }
        
        try {
            // Add client role by default
            user.addRole(new Role(Role.ROLE_CLIENT));
            
            // Create user
            User createdUser = userService.createUser(user, password);
            logger.info("User registered successfully: {}", createdUser.getUsername());
            
            redirectAttributes.addFlashAttribute("successMessage", "Registration successful! You can now log in.");
            return "redirect:/login";
        } catch (Exception e) {
            logger.error("Error registering user", e);
            bindingResult.reject("error.user", "An error occurred during registration. Please try again.");
            return "auth/register";
        }
    }
    
    /**
     * Access denied page
     *
     * @param model Model
     * @return View name
     */
    @GetMapping("/access-denied")
    public String accessDenied(Model model) {
        model.addAttribute("title", "Access Denied");
        return "auth/access-denied";
    }
} 