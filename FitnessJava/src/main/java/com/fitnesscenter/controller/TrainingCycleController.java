package com.fitnesscenter.controller;

import com.fitnesscenter.model.TrainingCycle;
import com.fitnesscenter.service.TrainingCycleService;
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
 * Controller for training cycle management
 */
@Controller
@RequestMapping("/training-cycles")
public class TrainingCycleController {
    private static final Logger logger = LogManager.getLogger(TrainingCycleController.class);
    
    private final TrainingCycleService trainingCycleService;
    
    @Autowired
    public TrainingCycleController(TrainingCycleService trainingCycleService) {
        this.trainingCycleService = trainingCycleService;
    }
    
    /**
     * List all training cycles
     *
     * @param model Model
     * @param page Page number (optional)
     * @param showAll Whether to show all cycles including inactive ones (for admins)
     * @return View name
     */
    @GetMapping
    public String listTrainingCycles(
            Model model,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "false") boolean showAll) {
        
        int pageSize = 6;
        
        if (showAll) {
            model.addAttribute("cycles", trainingCycleService.findAll(page, pageSize));
            model.addAttribute("totalCycles", trainingCycleService.countTrainingCycles());
        } else {
            model.addAttribute("cycles", trainingCycleService.findActive(page, pageSize));
            // Ideally we'd have a countActive method, but for simplicity we'll use the full count
            model.addAttribute("totalCycles", trainingCycleService.countTrainingCycles());
        }
        
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", (int) Math.ceil((double) trainingCycleService.countTrainingCycles() / pageSize));
        model.addAttribute("showAll", showAll);
        
        return "training-cycles/list";
    }
    
    /**
     * Show training cycle details
     *
     * @param id Training cycle ID
     * @param model Model
     * @return View name
     */
    @GetMapping("/{id}")
    public String viewTrainingCycle(@PathVariable Long id, Model model) {
        TrainingCycle cycle = trainingCycleService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid training cycle ID: " + id));
        model.addAttribute("cycle", cycle);
        return "training-cycles/view";
    }
    
    /**
     * Show training cycle creation form
     *
     * @param model Model
     * @return View name
     */
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("cycle", new TrainingCycle());
        return "training-cycles/create";
    }
    
    /**
     * Process training cycle creation
     *
     * @param cycle Training cycle to create
     * @param bindingResult Validation result
     * @param redirectAttributes Redirect attributes
     * @return Redirect URL
     */
    @PostMapping("/create")
    public String createTrainingCycle(
            @Valid @ModelAttribute("cycle") TrainingCycle cycle,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        
        // Check validation errors
        if (bindingResult.hasErrors()) {
            return "training-cycles/create";
        }
        
        try {
            // Create training cycle
            TrainingCycle createdCycle = trainingCycleService.createTrainingCycle(cycle);
            logger.info("Training cycle created successfully: {}", createdCycle.getId());
            
            redirectAttributes.addFlashAttribute("successMessage", "Training cycle created successfully.");
            return "redirect:/training-cycles/" + createdCycle.getId();
        } catch (Exception e) {
            logger.error("Error creating training cycle", e);
            bindingResult.reject("error.cycle", "An error occurred during creation. Please try again.");
            return "training-cycles/create";
        }
    }
    
    /**
     * Show training cycle edit form
     *
     * @param id Training cycle ID
     * @param model Model
     * @return View name
     */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        TrainingCycle cycle = trainingCycleService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid training cycle ID: " + id));
        model.addAttribute("cycle", cycle);
        return "training-cycles/edit";
    }
    
    /**
     * Process training cycle update
     *
     * @param id Training cycle ID
     * @param cycle Training cycle to update
     * @param bindingResult Validation result
     * @param redirectAttributes Redirect attributes
     * @return Redirect URL
     */
    @PostMapping("/{id}/edit")
    public String updateTrainingCycle(
            @PathVariable Long id,
            @Valid @ModelAttribute("cycle") TrainingCycle cycle,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        
        // Check validation errors
        if (bindingResult.hasErrors()) {
            return "training-cycles/edit";
        }
        
        try {
            // Ensure cycle ID is set
            cycle.setId(id);
            
            // Update cycle
            TrainingCycle updatedCycle = trainingCycleService.updateTrainingCycle(cycle);
            logger.info("Training cycle updated successfully: {}", updatedCycle.getId());
            
            redirectAttributes.addFlashAttribute("successMessage", "Training cycle updated successfully.");
            return "redirect:/training-cycles/" + id;
        } catch (Exception e) {
            logger.error("Error updating training cycle", e);
            bindingResult.reject("error.cycle", "An error occurred during update. Please try again.");
            return "training-cycles/edit";
        }
    }
    
    /**
     * Activate training cycle
     *
     * @param id Training cycle ID
     * @param redirectAttributes Redirect attributes
     * @return Redirect URL
     */
    @PostMapping("/{id}/activate")
    public String activateTrainingCycle(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            boolean activated = trainingCycleService.activateTrainingCycle(id);
            if (activated) {
                logger.info("Training cycle activated successfully: {}", id);
                redirectAttributes.addFlashAttribute("successMessage", "Training cycle activated successfully.");
            } else {
                logger.warn("Failed to activate training cycle: {}", id);
                redirectAttributes.addFlashAttribute("errorMessage", "Failed to activate training cycle.");
            }
        } catch (Exception e) {
            logger.error("Error activating training cycle", e);
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred while activating training cycle.");
        }
        
        return "redirect:/training-cycles/" + id;
    }
    
    /**
     * Deactivate training cycle
     *
     * @param id Training cycle ID
     * @param redirectAttributes Redirect attributes
     * @return Redirect URL
     */
    @PostMapping("/{id}/deactivate")
    public String deactivateTrainingCycle(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            boolean deactivated = trainingCycleService.deactivateTrainingCycle(id);
            if (deactivated) {
                logger.info("Training cycle deactivated successfully: {}", id);
                redirectAttributes.addFlashAttribute("successMessage", "Training cycle deactivated successfully.");
            } else {
                logger.warn("Failed to deactivate training cycle: {}", id);
                redirectAttributes.addFlashAttribute("errorMessage", "Failed to deactivate training cycle.");
            }
        } catch (Exception e) {
            logger.error("Error deactivating training cycle", e);
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred while deactivating training cycle.");
        }
        
        return "redirect:/training-cycles/" + id;
    }
    
    /**
     * Delete training cycle
     *
     * @param id Training cycle ID
     * @param redirectAttributes Redirect attributes
     * @return Redirect URL
     */
    @PostMapping("/{id}/delete")
    public String deleteTrainingCycle(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            boolean deleted = trainingCycleService.deleteTrainingCycle(id);
            if (deleted) {
                logger.info("Training cycle deleted successfully: {}", id);
                redirectAttributes.addFlashAttribute("successMessage", "Training cycle deleted successfully.");
            } else {
                logger.warn("Failed to delete training cycle: {}", id);
                redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete training cycle.");
            }
        } catch (Exception e) {
            logger.error("Error deleting training cycle", e);
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred while deleting training cycle.");
        }
        
        return "redirect:/training-cycles";
    }
} 