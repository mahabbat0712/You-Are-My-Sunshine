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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

/**
 * Controller for user management
 */
@Controller
@RequestMapping("/users")
public class UserController {
    private static final Logger logger = LogManager.getLogger(UserController.class);
    
    private final UserService userService;
    
    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    /**
     * List all users
     *
     * @param model Model
     * @param page Page number (optional)
     * @return View name
     */
    @GetMapping
    public String listUsers(Model model, @RequestParam(defaultValue = "1") int page) {
        int pageSize = 10;
        model.addAttribute("users", userService.findAll(page, pageSize));
        model.addAttribute("currentPage", page);
        model.addAttribute("totalUsers", userService.countUsers());
        model.addAttribute("totalPages", (int) Math.ceil((double) userService.countUsers() / pageSize));
        return "users/list";
    }
    
    /**
     * Show user details
     *
     * @param id User ID
     * @param model Model
     * @return View name
     */
    @GetMapping("/{id}")
    public String viewUser(@PathVariable Long id, Model model) {
        User user = userService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID: " + id));
        model.addAttribute("user", user);
        return "users/view";
    }
    
    /**
     * Show user registration form
     *
     * @param model Model
     * @return View name
     */
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "users/register";
    }
    
    /**
     * Process user registration
     *
     * @param user User to register
     * @param bindingResult Validation result
     * @param password Raw password
     * @param redirectAttributes Redirect attributes
     * @return Redirect URL
     */
    @PostMapping("/register")
    public String registerUser(
            @Valid @ModelAttribute("user") User user,
            BindingResult bindingResult,
            @RequestParam String password,
            RedirectAttributes redirectAttributes) {
        
        // Check validation errors
        if (bindingResult.hasErrors()) {
            return "users/register";
        }
        
        // Check if username or email already exists
        if (userService.usernameExists(user.getUsername())) {
            bindingResult.rejectValue("username", "error.user", "Username already exists");
            return "users/register";
        }
        
        if (userService.emailExists(user.getEmail())) {
            bindingResult.rejectValue("email", "error.user", "Email already exists");
            return "users/register";
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
            return "users/register";
        }
    }
    
    /**
     * Show user edit form
     *
     * @param id User ID
     * @param model Model
     * @return View name
     */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        User user = userService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID: " + id));
        model.addAttribute("user", user);
        return "users/edit";
    }
    
    /**
     * Process user update
     *
     * @param id User ID
     * @param user User to update
     * @param bindingResult Validation result
     * @param redirectAttributes Redirect attributes
     * @return Redirect URL
     */
    @PostMapping("/{id}/edit")
    public String updateUser(
            @PathVariable Long id,
            @Valid @ModelAttribute("user") User user,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        
        // Check validation errors
        if (bindingResult.hasErrors()) {
            return "users/edit";
        }
        
        try {
            // Ensure user ID is set
            user.setId(id);
            
            // Update user
            User updatedUser = userService.updateUser(user);
            logger.info("User updated successfully: {}", updatedUser.getUsername());
            
            redirectAttributes.addFlashAttribute("successMessage", "User updated successfully.");
            return "redirect:/users/" + id;
        } catch (Exception e) {
            logger.error("Error updating user", e);
            bindingResult.reject("error.user", "An error occurred during update. Please try again.");
            return "users/edit";
        }
    }
    
    /**
     * Delete user
     *
     * @param id User ID
     * @param redirectAttributes Redirect attributes
     * @return Redirect URL
     */
    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            boolean deleted = userService.deleteUser(id);
            if (deleted) {
                logger.info("User deleted successfully: {}", id);
                redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully.");
            } else {
                logger.warn("Failed to delete user: {}", id);
                redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete user.");
            }
        } catch (Exception e) {
            logger.error("Error deleting user", e);
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred while deleting user.");
        }
        
        return "redirect:/users";
    }
} 