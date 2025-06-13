package com.fitnesscenter.controller;

import com.fitnesscenter.model.User;
import com.fitnesscenter.service.SessionService;
import com.fitnesscenter.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

/**
 * Controller for user management in admin panel
 */
@Controller
@RequestMapping("/admin/user-management")
public class AdminUserController {
    private static final Logger logger = LogManager.getLogger(AdminUserController.class);
    
    private final UserService userService;
    private final SessionService sessionService;
    
    @Autowired
    public AdminUserController(UserService userService, SessionService sessionService) {
        this.userService = userService;
        this.sessionService = sessionService;
        logger.info("AdminUserController initialized");
    }
    
    /**
     * Get current authenticated user from session
     */
    private User getCurrentUser(HttpServletRequest request) {
        return sessionService.getCurrentUser(request);
    }
    
    /**
     * Check if user has admin role
     */
    private boolean checkAdminAccess(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            return false;
        }
        
        if (!currentUser.hasRole("ADMIN")) {
            redirectAttributes.addFlashAttribute("error", "У вас нет доступа к управлению пользователями");
            return false;
        }
        
        return true;
    }
    
    /**
     * List all users
     */
    @GetMapping
    public String listUsers(HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        if (!checkAdminAccess(request, redirectAttributes)) {
            return "redirect:/";
        }
        
        List<User> users = userService.findAllUsers();
        model.addAttribute("users", users);
        
        return "admin/users/list";
    }
    
    /**
     * View user details
     */
    @GetMapping("/{id}")
    public String viewUser(@PathVariable Long id, HttpServletRequest request, 
                          Model model, RedirectAttributes redirectAttributes) {
        if (!checkAdminAccess(request, redirectAttributes)) {
            return "redirect:/";
        }
        
        User user = userService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID: " + id));
        model.addAttribute("user", user);
        
        return "admin/users/view";
    }
    
    /**
     * Show edit user form
     */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, HttpServletRequest request,
                              Model model, RedirectAttributes redirectAttributes) {
        if (!checkAdminAccess(request, redirectAttributes)) {
            return "redirect:/";
        }
        
        User user = userService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID: " + id));
        model.addAttribute("user", user);
        
        return "admin/users/edit";
    }
    
    /**
     * Update user
     */
    @PostMapping("/{id}/edit")
    public String updateUser(@PathVariable Long id, @Valid @ModelAttribute User user,
                            BindingResult bindingResult, HttpServletRequest request,
                            Model model, RedirectAttributes redirectAttributes) {
        if (!checkAdminAccess(request, redirectAttributes)) {
            return "redirect:/";
        }
        
        if (bindingResult.hasErrors()) {
            return "admin/users/edit";
        }
        
        try {
            User existingUser = userService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid user ID: " + id));
            
            // Update only allowed fields
            existingUser.setFirstName(user.getFirstName());
            existingUser.setLastName(user.getLastName());
            existingUser.setEmail(user.getEmail());
            existingUser.setPhone(user.getPhone());
            existingUser.setRoles(user.getRoles());
            
            userService.updateUser(existingUser);
            redirectAttributes.addFlashAttribute("success", "Пользователь успешно обновлен");
            return "redirect:/admin/user-management/" + id;
        } catch (Exception e) {
            logger.error("Error updating user", e);
            redirectAttributes.addFlashAttribute("error", "Ошибка при обновлении пользователя");
            return "redirect:/admin/user-management/" + id + "/edit";
        }
    }
} 