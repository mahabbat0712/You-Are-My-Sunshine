package com.fitnesscenter.controller;

import com.fitnesscenter.model.Role;
import com.fitnesscenter.model.User;
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
    
    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
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
            Model model) {
        
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
                
                // Create session
                HttpSession session = request.getSession(true);
                session.setAttribute("user", user);
                session.setAttribute("userId", user.getId());
                session.setAttribute("username", user.getUsername());
                
                // Set user roles in session
                boolean isAdmin = user.getRoles().stream()
                        .anyMatch(role -> role.getName().equals(Role.ROLE_ADMIN));
                boolean isTrainer = user.getRoles().stream()
                        .anyMatch(role -> role.getName().equals(Role.ROLE_TRAINER));
                boolean isClient = user.getRoles().stream()
                        .anyMatch(role -> role.getName().equals(Role.ROLE_CLIENT));
                
                session.setAttribute("isAdmin", isAdmin);
                session.setAttribute("isTrainer", isTrainer);
                session.setAttribute("isClient", isClient);
                
                logger.info("User logged in: {}", username);
                return "redirect:/";
            } else {
                redirectAttributes.addAttribute("error", "true");
                return "redirect:/login";
            }
        } catch (Exception e) {
            logger.error("Login error", e);
            redirectAttributes.addAttribute("error", "true");
            return "redirect:/login";
        }
    }
    
    /**
     * Process logout
     *
     * @param request HTTP request
     * @return Redirect URL
     */
    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            String username = (String) session.getAttribute("username");
            logger.info("User logged out: {}", username);
            session.invalidate();
        }
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