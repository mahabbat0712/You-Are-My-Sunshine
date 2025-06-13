package com.fitnesscenter.controller;

import com.fitnesscenter.model.AccountType;
import com.fitnesscenter.model.ClientProfile;
import com.fitnesscenter.model.User;
import com.fitnesscenter.service.DiscountService;
import com.fitnesscenter.service.SessionService;
import com.fitnesscenter.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;

/**
 * Controller for managing user profiles
 */
@Controller
@RequestMapping("/profile")
public class ProfileController {

    private static final Logger logger = LogManager.getLogger(ProfileController.class);
    
    private final UserService userService;
    private final DiscountService discountService;
    private final SessionService sessionService;
    
    @Autowired
    public ProfileController(UserService userService, DiscountService discountService, SessionService sessionService) {
        this.userService = userService;
        this.discountService = discountService;
        this.sessionService = sessionService;
        logger.info("ProfileController initialized");
    }
    
    /**
     * Get current authenticated user from session
     */
    private User getCurrentUser(HttpServletRequest request) {
        return sessionService.getCurrentUser(request);
    }
    
    /**
     * View user profile
     */
    @GetMapping
    public String viewProfile(HttpServletRequest request, Model model) {
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        Optional<ClientProfile> clientProfileOpt = userService.getClientProfile(currentUser.getId());
        
        model.addAttribute("user", currentUser);
        
        if (currentUser.hasRole("CLIENT") && clientProfileOpt.isPresent()) {
            ClientProfile profile = clientProfileOpt.get();
            model.addAttribute("clientProfile", profile);
            model.addAttribute("accountTypes", discountService.findAllAccountTypes());
            
            // Calculate user's discount percentage
            BigDecimal discountPercentage = discountService.calculateDiscountForClient(currentUser.getId());
            model.addAttribute("userDiscount", discountPercentage);
            model.addAttribute("hasDiscount", discountPercentage.compareTo(BigDecimal.ZERO) > 0);
        }
        
        return "profile/view";
    }
    
    /**
     * Display form to edit user profile
     */
    @GetMapping("/edit")
    public String editProfileForm(HttpServletRequest request, Model model) {
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("user", currentUser);
        
        if (currentUser.hasRole("CLIENT")) {
            Optional<ClientProfile> clientProfileOpt = userService.getClientProfile(currentUser.getId());
            ClientProfile profile = clientProfileOpt.orElse(new ClientProfile());
            if (profile.getUser() == null) {
                profile.setUser(currentUser);
            }
            
            model.addAttribute("clientProfile", profile);
            model.addAttribute("accountTypes", discountService.findAllAccountTypes());
        }
        
        return "profile/edit";
    }
    
    /**
     * Process user profile edit
     */
    @PostMapping("/edit")
    public String updateProfile(HttpServletRequest request,
                               @Valid @ModelAttribute("user") User user,
                               BindingResult userResult,
                               ClientProfile clientProfile,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        // Prevent changing username and email
        user.setUsername(currentUser.getUsername());
        user.setEmail(currentUser.getEmail());
        user.setId(currentUser.getId());
        user.setRoles(currentUser.getRoles());
        user.setActive(currentUser.isActive());
        user.setCreatedAt(currentUser.getCreatedAt());
        
        // Empty password means no change
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            user.setPassword(currentUser.getPassword());
        } else {
            // Password will be encoded in the service
        }
        
        if (userResult.hasErrors()) {
            model.addAttribute("accountTypes", discountService.findAllAccountTypes());
            return "profile/edit";
        }
        
        try {
            // Update user information
            userService.saveUser(user);
            
            // Update client profile if user is a client
            if (currentUser.hasRole("CLIENT")) {
                // Load existing client profile if available to preserve any values not in form
                Optional<ClientProfile> existingProfile = userService.getClientProfile(currentUser.getId());
                if (existingProfile.isPresent()) {
                    // Set ID to ensure update instead of insert
                    if (clientProfile.getId() == null) {
                        clientProfile.setId(existingProfile.get().getId());
                    }
                    
                    // Preserve account type if not provided
                    if (clientProfile.getAccountType() == null && existingProfile.get().getAccountType() != null) {
                        clientProfile.setAccountType(existingProfile.get().getAccountType());
                    }
                    
                    // Preserve completed cycles if not provided
                    if (clientProfile.getCompletedCycles() == null || clientProfile.getCompletedCycles() == 0) {
                        clientProfile.setCompletedCycles(existingProfile.get().getCompletedCycles());
                    }
                }
                
                // Ensure user association
                clientProfile.setUser(user);
                userService.saveClientProfile(clientProfile);
            }
            
            // Update session with updated user
            request.getSession().setAttribute("user", user);
            
            redirectAttributes.addFlashAttribute("success", "Your profile has been updated successfully!");
            return "redirect:/profile";
        } catch (Exception e) {
            logger.error("Error updating profile", e);
            model.addAttribute("error", "Failed to update profile: " + e.getMessage());
            model.addAttribute("accountTypes", discountService.findAllAccountTypes());
            return "profile/edit";
        }
    }
    
    /**
     * Display form to change password
     */
    @GetMapping("/change-password")
    public String changePasswordForm(HttpServletRequest request) {
        if (getCurrentUser(request) == null) {
            return "redirect:/login";
        }
        return "profile/change-password";
    }
    
    /**
     * Process password change
     */
    @PostMapping("/change-password")
    public String changePassword(HttpServletRequest request,
                                String currentPassword, 
                                String newPassword,
                                String confirmPassword,
                                RedirectAttributes redirectAttributes) {
        
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        if (newPassword == null || newPassword.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "New password cannot be empty");
            return "redirect:/profile/change-password";
        }
        
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "New passwords do not match");
            return "redirect:/profile/change-password";
        }
        
        try {
            boolean changed = userService.changePassword(currentUser.getId(), currentPassword, newPassword);
            if (changed) {
                redirectAttributes.addFlashAttribute("success", "Password changed successfully");
                return "redirect:/profile";
            } else {
                redirectAttributes.addFlashAttribute("error", "Current password is incorrect");
                return "redirect:/profile/change-password";
            }
        } catch (Exception e) {
            logger.error("Error changing password", e);
            redirectAttributes.addFlashAttribute("error", "Failed to change password: " + e.getMessage());
            return "redirect:/profile/change-password";
        }
    }

    /**
     * Become a client - add CLIENT role to the current user
     */
    @PostMapping("/become-client")
    public String becomeClient(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        // Проверяем, не является ли пользователь уже клиентом
        if (currentUser.hasRole("CLIENT")) {
            redirectAttributes.addFlashAttribute("info", "Вы уже являетесь клиентом");
            return "redirect:/profile";
        }
        
        try {
            // Добавляем роль клиента
            boolean success = userService.assignRole(currentUser.getId(), "CLIENT");
            
            if (success) {
                // Обновляем сессию с новой ролью
                Optional<User> updatedUserOpt = userService.findById(currentUser.getId());
                if (updatedUserOpt.isPresent()) {
                    User updatedUser = updatedUserOpt.get();
                    sessionService.createSession(request, updatedUser);
                    
                    redirectAttributes.addFlashAttribute("success", "Вы успешно стали клиентом и теперь можете покупать тренировочные программы");
                } else {
                    redirectAttributes.addFlashAttribute("error", "Не удалось обновить сессию");
                }
            } else {
                redirectAttributes.addFlashAttribute("error", "Не удалось добавить роль клиента");
            }
            
            return "redirect:/profile";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при добавлении роли клиента: " + e.getMessage());
            return "redirect:/profile";
        }
    }
} 