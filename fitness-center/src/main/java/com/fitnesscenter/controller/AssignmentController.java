package com.fitnesscenter.controller;

import com.fitnesscenter.model.Assignment;
import com.fitnesscenter.model.AssignmentCategory;
import com.fitnesscenter.model.Equipment;
import com.fitnesscenter.model.Order;
import com.fitnesscenter.model.User;
import com.fitnesscenter.service.AssignmentService;
import com.fitnesscenter.service.EquipmentService;
import com.fitnesscenter.service.OrderService;
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
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controller for managing assignments
 */
@Controller
@RequestMapping("/assignments")
public class AssignmentController {

    private static final Logger logger = LogManager.getLogger(AssignmentController.class);
    
    private final AssignmentService assignmentService;
    private final UserService userService;
    private final OrderService orderService;
    private final EquipmentService equipmentService;
    private final SessionService sessionService;
    
    @Autowired
    public AssignmentController(
            AssignmentService assignmentService, 
            UserService userService, 
            OrderService orderService,
            EquipmentService equipmentService,
            SessionService sessionService) {
        this.assignmentService = assignmentService;
        this.userService = userService;
        this.orderService = orderService;
        this.equipmentService = equipmentService;
        this.sessionService = sessionService;
        logger.info("AssignmentController initialized");
    }
    
    /**
     * Get current authenticated user from session
     */
    private User getCurrentUser(HttpServletRequest request) {
        return sessionService.getCurrentUser(request);
    }
    
    /**
     * List all assignments for current user
     */
    @GetMapping
    public String listAssignments(HttpServletRequest request, Model model) {
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            // Сохраняем намерение пользователя посмотреть задания
            HttpSession session = request.getSession(true);
            session.setAttribute("intendedDestination", "/assignments");
            
            return "redirect:/login";
        }
        
        List<Assignment> assignments;
        
        // Initialize role variables to false by default
        model.addAttribute("isAdmin", false);
        model.addAttribute("isTrainer", false);
        
        // Get different assignments based on user role
        if (currentUser.hasRole("ADMIN")) {
            assignments = assignmentService.findAllAssignments();
            model.addAttribute("isAdmin", true);
        } else if (currentUser.hasRole("TRAINER")) {
            assignments = assignmentService.findAssignmentsByTrainer(currentUser.getId());
            model.addAttribute("isTrainer", true);
        } else {
            assignments = assignmentService.findAssignmentsByClient(currentUser.getId());
        }
        
        model.addAttribute("assignments", assignments);
        return "assignments/list";
    }
    
    /**
     * View assignment details
     */
    @GetMapping("/{id}")
    public String viewAssignment(@PathVariable("id") Long id, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            // Сохраняем намерение пользователя просмотреть задание
            HttpSession session = request.getSession(true);
            session.setAttribute("intendedDestination", "/assignments/" + id);
            
            return "redirect:/login";
        }
        
        Optional<Assignment> assignmentOpt = assignmentService.findAssignmentById(id);
        if (!assignmentOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Assignment not found");
            return "redirect:/assignments";
        }
        
        Assignment assignment = assignmentOpt.get();
        
        // Initialize role variables to false by default
        model.addAttribute("isAdmin", false);
        model.addAttribute("isTrainer", false);
        model.addAttribute("isClient", false);
        
        // Set roles based on the current user
        if (currentUser.hasRole("ADMIN")) {
            model.addAttribute("isAdmin", true);
        } else if (currentUser.hasRole("TRAINER")) {
            model.addAttribute("isTrainer", true);
        } else {
            model.addAttribute("isClient", true);
        }
        
        // Security check: Only client assigned to this assignment, its trainer, or admin can view
        boolean isAuthorized = currentUser.hasRole("ADMIN") ||
                              (assignment.getTrainer() != null && assignment.getTrainer().getId().equals(currentUser.getId())) ||
                              (assignment.getOrder() != null && assignment.getOrder().getClient() != null && 
                               assignment.getOrder().getClient().getId().equals(currentUser.getId()));
        
        if (!isAuthorized) {
            redirectAttributes.addFlashAttribute("error", "You are not authorized to view this assignment");
            return "redirect:/assignments";
        }
        
        model.addAttribute("assignment", assignment);
        return "assignments/view";
    }
    
    /**
     * Show form to create new assignment (only for trainers)
     */
    @GetMapping("/create")
    public String createAssignmentForm(
            @RequestParam(value = "orderId", required = false) Long orderId,
            HttpServletRequest request, 
            Model model,
            RedirectAttributes redirectAttributes) {
        
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        // Initialize role variables to false by default
        model.addAttribute("isAdmin", false);
        model.addAttribute("isTrainer", false);
        model.addAttribute("isClient", false);
        
        // Only trainers and admins can create assignments
        if (currentUser.hasRole("ADMIN")) {
            model.addAttribute("isAdmin", true);
        } else if (currentUser.hasRole("TRAINER")) {
            model.addAttribute("isTrainer", true);
        } else {
            redirectAttributes.addFlashAttribute("error", "Only trainers can create assignments");
            return "redirect:/assignments";
        }
        
        // Get orders assigned to this trainer
        List<Order> assignableOrders = orderService.findOrdersAssignedToTrainer(currentUser.getId())
                .stream()
                .filter(o -> o.getStatus() == Order.OrderStatus.PAID)
                .collect(Collectors.toList());
        
        // If no orders found
        if (assignableOrders.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "You don't have any paid orders to create assignments for");
            return "redirect:/assignments";
        }
        
        // If orderId is provided, pre-select it
        if (orderId != null) {
            Optional<Order> orderOpt = orderService.findOrderById(orderId);
            if (orderOpt.isPresent() && 
                orderOpt.get().getTrainer() != null && 
                orderOpt.get().getTrainer().getId().equals(currentUser.getId()) &&
                orderOpt.get().getStatus() == Order.OrderStatus.PAID) {
                model.addAttribute("selectedOrderId", orderId);
            }
        }
        
        // Get available equipment
        List<Equipment> availableEquipment = equipmentService.findAllEquipment();
        
        // Create assignment categories
        List<AssignmentCategory> categories = new ArrayList<>();
        categories.add(new AssignmentCategory(1, AssignmentCategory.CATEGORY_EXERCISE));
        categories.add(new AssignmentCategory(2, AssignmentCategory.CATEGORY_EQUIPMENT));
        categories.add(new AssignmentCategory(3, AssignmentCategory.CATEGORY_DIET));
        
        // Add attributes to model
        model.addAttribute("assignment", new Assignment());
        model.addAttribute("assignableOrders", assignableOrders);
        model.addAttribute("availableEquipment", availableEquipment);
        model.addAttribute("categories", categories);
        
        return "assignments/create";
    }
    
    /**
     * Process assignment creation
     */
    @PostMapping("/create")
    public String createAssignment(
            @RequestParam("orderId") Long orderId,
            @RequestParam("categoryId") Integer categoryId,
            @RequestParam(value = "equipmentIds", required = false) List<Long> equipmentIds,
            @ModelAttribute Assignment assignment,
            BindingResult bindingResult,
            HttpServletRequest request,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        // Log request parameters for debugging
        logger.info("POST request to /assignments/create received");
        logger.info("orderId: {}", orderId);
        logger.info("categoryId: {}", categoryId);
        logger.info("equipmentIds: {}", equipmentIds);
        logger.info("assignment: {}", assignment);
        
        User currentUser = getCurrentUser(request);
        if (currentUser == null || !currentUser.hasRole("TRAINER")) {
            logger.warn("Unauthorized user attempt to create assignment: {}", currentUser);
            redirectAttributes.addFlashAttribute("error", "Unauthorized");
            return "redirect:/assignments";
        }
        
        // Check if order exists and is assigned to this trainer
        Optional<Order> orderOpt = orderService.findOrderById(orderId);
        if (!orderOpt.isPresent()) {
            logger.warn("Order not found: {}", orderId);
            redirectAttributes.addFlashAttribute("error", "Order not found");
            return "redirect:/assignments/create";
        }
        
        Order order = orderOpt.get();
        
        // Check if order is assigned to this trainer
        if (order.getTrainer() == null || !order.getTrainer().getId().equals(currentUser.getId())) {
            redirectAttributes.addFlashAttribute("error", "You are not authorized to create assignments for this order");
            return "redirect:/assignments/create";
        }
        
        // Check if order is in PAID status
        if (order.getStatus() != Order.OrderStatus.PAID) {
            redirectAttributes.addFlashAttribute("error", "You can only create assignments for paid orders");
            return "redirect:/assignments/create";
        }
        
        // Set assignment category
        AssignmentCategory category = new AssignmentCategory();
        category.setId(categoryId);
        switch(categoryId) {
            case 1:
                category.setName(AssignmentCategory.CATEGORY_EXERCISE);
                break;
            case 2:
                category.setName(AssignmentCategory.CATEGORY_EQUIPMENT);
                break;
            case 3:
                category.setName(AssignmentCategory.CATEGORY_DIET);
                break;
            default:
                category.setName(AssignmentCategory.CATEGORY_EXERCISE);
        }

        // Set required fields before validation
        assignment.setOrder(order);
        assignment.setTrainer(currentUser);
        assignment.setCategory(category);
        
        // Now perform validation manually
        javax.validation.Validator validator = javax.validation.Validation.buildDefaultValidatorFactory().getValidator();
        Set<javax.validation.ConstraintViolation<Assignment>> violations = validator.validate(assignment);
        
        for (javax.validation.ConstraintViolation<Assignment> violation : violations) {
            String propertyPath = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            bindingResult.rejectValue(propertyPath, "", message);
        }
        
        // Log validation errors if any
        if (bindingResult.hasErrors()) {
            logger.warn("Binding errors: {}", bindingResult.getAllErrors());
            
            // Re-populate the form data
            List<Order> assignableOrders = orderService.findOrdersAssignedToTrainer(currentUser.getId())
                    .stream()
                    .filter(o -> o.getStatus() == Order.OrderStatus.PAID)
                    .collect(Collectors.toList());
            
            List<Equipment> availableEquipment = equipmentService.findAllEquipment();
            List<AssignmentCategory> categories = new ArrayList<>();
            categories.add(new AssignmentCategory(1, AssignmentCategory.CATEGORY_EXERCISE));
            categories.add(new AssignmentCategory(2, AssignmentCategory.CATEGORY_EQUIPMENT));
            categories.add(new AssignmentCategory(3, AssignmentCategory.CATEGORY_DIET));
            
            model.addAttribute("assignableOrders", assignableOrders);
            model.addAttribute("availableEquipment", availableEquipment);
            model.addAttribute("categories", categories);
            
            return "assignments/create";
        }
        
        // Set equipment if any
        if (equipmentIds != null && !equipmentIds.isEmpty()) {
            Set<Equipment> equipment = equipmentService.findEquipmentByIds(equipmentIds);
            assignment.setRequiredEquipment(equipment);
        }
        
        // Save assignment
        Assignment savedAssignment = assignmentService.saveAssignment(assignment);
        if (savedAssignment != null) {
            logger.info("Assignment saved successfully with ID: {}", savedAssignment.getId());
            redirectAttributes.addFlashAttribute("success", "Assignment created successfully");
            return "redirect:/assignments/" + savedAssignment.getId();
        } else {
            logger.error("Failed to save assignment - service returned null");
            redirectAttributes.addFlashAttribute("error", "Failed to create assignment");
            return "redirect:/assignments/create";
        }
    }
    
    /**
     * Show form to edit an assignment
     */
    @GetMapping("/{id}/edit")
    public String editAssignmentForm(
            @PathVariable("id") Long id,
            HttpServletRequest request,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        // Initialize role variables to false by default
        model.addAttribute("isAdmin", false);
        model.addAttribute("isTrainer", false);
        
        // Set roles based on the current user
        if (currentUser.hasRole("ADMIN")) {
            model.addAttribute("isAdmin", true);
        } else if (currentUser.hasRole("TRAINER")) {
            model.addAttribute("isTrainer", true);
        }
        
        Optional<Assignment> assignmentOpt = assignmentService.findAssignmentById(id);
        if (!assignmentOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Assignment not found");
            return "redirect:/assignments";
        }
        
        Assignment assignment = assignmentOpt.get();
        
        // Only the trainer who created the assignment or admin can edit it
        boolean isAuthorized = currentUser.hasRole("ADMIN") ||
                              (assignment.getTrainer() != null && assignment.getTrainer().getId().equals(currentUser.getId()));
        
        if (!isAuthorized) {
            redirectAttributes.addFlashAttribute("error", "You are not authorized to edit this assignment");
            return "redirect:/assignments";
        }
        
        // Get available equipment
        List<Equipment> availableEquipment = equipmentService.findAllEquipment();
        
        model.addAttribute("assignment", assignment);
        model.addAttribute("availableEquipment", availableEquipment);
        
        return "assignments/edit";
    }
    
    /**
     * Process assignment update
     */
    @PostMapping("/{id}/update")
    public String updateAssignment(
            @PathVariable("id") Long id,
            @RequestParam(value = "equipmentIds", required = false) List<Long> equipmentIds,
            @ModelAttribute Assignment assignment,
            BindingResult bindingResult,
            HttpServletRequest request,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        Optional<Assignment> existingAssignmentOpt = assignmentService.findAssignmentById(id);
        if (!existingAssignmentOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Assignment not found");
            return "redirect:/assignments";
        }
        
        Assignment existingAssignment = existingAssignmentOpt.get();
        
        // Only the trainer who created the assignment or admin can edit it
        boolean isAuthorized = currentUser.hasRole("ADMIN") ||
                              (existingAssignment.getTrainer() != null && 
                               existingAssignment.getTrainer().getId().equals(currentUser.getId()));
        
        if (!isAuthorized) {
            redirectAttributes.addFlashAttribute("error", "You are not authorized to edit this assignment");
            return "redirect:/assignments";
        }
        
        // Preserve the required fields from the existing assignment
        assignment.setId(existingAssignment.getId());
        assignment.setOrder(existingAssignment.getOrder());
        assignment.setTrainer(existingAssignment.getTrainer());
        assignment.setCategory(existingAssignment.getCategory());
        assignment.setCreatedAt(existingAssignment.getCreatedAt());
        assignment.setStatus(existingAssignment.getStatus());
        
        // Now validate the assignment
        javax.validation.Validator validator = javax.validation.Validation.buildDefaultValidatorFactory().getValidator();
        Set<javax.validation.ConstraintViolation<Assignment>> violations = validator.validate(assignment);
        
        for (javax.validation.ConstraintViolation<Assignment> violation : violations) {
            String propertyPath = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            bindingResult.rejectValue(propertyPath, "", message);
        }
        
        if (bindingResult.hasErrors()) {
            logger.warn("Binding errors during update: {}", bindingResult.getAllErrors());
            
            // Re-populate the form data
            List<Equipment> availableEquipment = equipmentService.findAllEquipment();
            model.addAttribute("availableEquipment", availableEquipment);
            model.addAttribute("assignment", existingAssignment); // Use the existing assignment data
            
            return "assignments/edit";
        }
        
        // Update the existing assignment with the new values
        existingAssignment.setTitle(assignment.getTitle());
        existingAssignment.setDescription(assignment.getDescription());
        existingAssignment.setSchedule(assignment.getSchedule());
        existingAssignment.setUpdatedAt(LocalDateTime.now());
        
        // Update equipment if any
        if (equipmentIds != null) {
            Set<Equipment> equipment = equipmentService.findEquipmentByIds(equipmentIds);
            existingAssignment.setRequiredEquipment(equipment);
        } else {
            existingAssignment.getRequiredEquipment().clear();
        }
        
        // Save updated assignment
        Assignment updatedAssignment = assignmentService.updateAssignment(existingAssignment);
        if (updatedAssignment != null) {
            redirectAttributes.addFlashAttribute("success", "Assignment updated successfully");
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to update assignment");
        }
        
        return "redirect:/assignments/" + id;
    }
    
    /**
     * Delete an assignment
     */
    @PostMapping("/{id}/delete")
    public String deleteAssignment(
            @PathVariable("id") Long id,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        Optional<Assignment> assignmentOpt = assignmentService.findAssignmentById(id);
        if (!assignmentOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Assignment not found");
            return "redirect:/assignments";
        }
        
        Assignment assignment = assignmentOpt.get();
        
        // Only the trainer who created the assignment or admin can delete it
        boolean isAuthorized = currentUser.hasRole("ADMIN") ||
                              (assignment.getTrainer() != null && 
                               assignment.getTrainer().getId().equals(currentUser.getId()));
        
        if (!isAuthorized) {
            redirectAttributes.addFlashAttribute("error", "You are not authorized to delete this assignment");
            return "redirect:/assignments";
        }
        
        boolean deleted = assignmentService.deleteAssignment(id);
        if (deleted) {
            redirectAttributes.addFlashAttribute("success", "Assignment deleted successfully");
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to delete assignment");
        }
        
        return "redirect:/assignments";
    }
    
    /**
     * Client accepts an assignment
     */
    @PostMapping("/{id}/accept")
    public String acceptAssignment(
            @PathVariable("id") Long id,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        Optional<Assignment> assignmentOpt = assignmentService.findAssignmentById(id);
        if (!assignmentOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Assignment not found");
            return "redirect:/assignments";
        }
        
        Assignment assignment = assignmentOpt.get();
        
        // Only the client assigned to this assignment can accept it
        boolean isAuthorized = assignment.getOrder() != null && 
                              assignment.getOrder().getClient() != null &&
                              assignment.getOrder().getClient().getId().equals(currentUser.getId());
        
        if (!isAuthorized) {
            redirectAttributes.addFlashAttribute("error", "You are not authorized to accept this assignment");
            return "redirect:/assignments";
        }
        
        // Check if assignment is in ASSIGNED status
        if (!"ASSIGNED".equals(assignment.getStatus())) {
            redirectAttributes.addFlashAttribute("error", "This assignment cannot be accepted because it's not in assigned status");
            return "redirect:/assignments/" + id;
        }
        
        // Update status to ACCEPTED
        assignment.setStatus("ACCEPTED");
        Assignment updatedAssignment = assignmentService.updateAssignment(assignment);
        
        if (updatedAssignment != null) {
            redirectAttributes.addFlashAttribute("success", "Assignment accepted successfully");
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to accept assignment");
        }
        
        return "redirect:/assignments/" + id;
    }
    
    /**
     * Client rejects an assignment
     */
    @PostMapping("/{id}/reject")
    public String rejectAssignment(
            @PathVariable("id") Long id,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        Optional<Assignment> assignmentOpt = assignmentService.findAssignmentById(id);
        if (!assignmentOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Assignment not found");
            return "redirect:/assignments";
        }
        
        Assignment assignment = assignmentOpt.get();
        
        // Only the client assigned to this assignment can reject it
        boolean isAuthorized = assignment.getOrder() != null && 
                              assignment.getOrder().getClient() != null &&
                              assignment.getOrder().getClient().getId().equals(currentUser.getId());
        
        if (!isAuthorized) {
            redirectAttributes.addFlashAttribute("error", "You are not authorized to reject this assignment");
            return "redirect:/assignments";
        }
        
        // Check if assignment is in ASSIGNED status
        if (!"ASSIGNED".equals(assignment.getStatus())) {
            redirectAttributes.addFlashAttribute("error", "This assignment cannot be rejected because it's not in assigned status");
            return "redirect:/assignments/" + id;
        }
        
        // Update status to REJECTED
        assignment.setStatus("REJECTED");
        Assignment updatedAssignment = assignmentService.updateAssignment(assignment);
        
        if (updatedAssignment != null) {
            redirectAttributes.addFlashAttribute("success", "Assignment rejected successfully");
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to reject assignment");
        }
        
        return "redirect:/assignments/" + id;
    }
    
    /**
     * Client marks an assignment as completed
     */
    @PostMapping("/{id}/complete")
    public String completeAssignment(
            @PathVariable("id") Long id,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        Optional<Assignment> assignmentOpt = assignmentService.findAssignmentById(id);
        if (!assignmentOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Assignment not found");
            return "redirect:/assignments";
        }
        
        Assignment assignment = assignmentOpt.get();
        
        // Only the client assigned to this assignment can mark it as completed
        boolean isAuthorized = assignment.getOrder() != null && 
                              assignment.getOrder().getClient() != null &&
                              assignment.getOrder().getClient().getId().equals(currentUser.getId());
        
        if (!isAuthorized) {
            redirectAttributes.addFlashAttribute("error", "You are not authorized to complete this assignment");
            return "redirect:/assignments";
        }
        
        // Check if assignment is in ACCEPTED status
        if (!"ACCEPTED".equals(assignment.getStatus())) {
            redirectAttributes.addFlashAttribute("error", "This assignment cannot be completed because it's not in accepted status");
            return "redirect:/assignments/" + id;
        }
        
        // Update status to COMPLETED
        assignment.setStatus("COMPLETED");
        Assignment updatedAssignment = assignmentService.updateAssignment(assignment);
        
        if (updatedAssignment != null) {
            redirectAttributes.addFlashAttribute("success", "Assignment marked as completed successfully");
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to mark assignment as completed");
        }
        
        return "redirect:/assignments/" + id;
    }
} 