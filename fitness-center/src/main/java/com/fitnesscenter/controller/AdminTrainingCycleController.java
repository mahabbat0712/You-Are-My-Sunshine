package com.fitnesscenter.controller;

import com.fitnesscenter.model.TrainingCycle;
import com.fitnesscenter.model.User;
import com.fitnesscenter.service.SessionService;
import com.fitnesscenter.service.TrainingCycleService;
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
 * Controller for training cycle management in admin panel
 */
@Controller
@RequestMapping("/admin/training-cycles")
public class AdminTrainingCycleController {
    private static final Logger logger = LogManager.getLogger(AdminTrainingCycleController.class);
    
    private final TrainingCycleService trainingCycleService;
    private final SessionService sessionService;
    
    @Autowired
    public AdminTrainingCycleController(TrainingCycleService trainingCycleService, SessionService sessionService) {
        this.trainingCycleService = trainingCycleService;
        this.sessionService = sessionService;
        logger.info("AdminTrainingCycleController initialized");
    }
    
    /**
     * Get current authenticated user from session
     */
    private User getCurrentUser(HttpServletRequest request) {
        return sessionService.getCurrentUser(request);
    }
    
    /**
     * List all training cycles
     */
    @GetMapping
    public String listTrainingCycles(
            HttpServletRequest request,
            Model model,
            RedirectAttributes redirectAttributes,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String query) {
        
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        if (!currentUser.hasRole("ADMIN")) {
            redirectAttributes.addFlashAttribute("error", "У вас нет доступа к управлению тренировочными циклами");
            return "redirect:/";
        }
        
        try {
            // Параметры пагинации
            int pageSize = 10;
            // Ensure page is positive to avoid negative OFFSET
            page = Math.max(1, page);
            int offset = (page - 1) * pageSize;
            
            List<TrainingCycle> trainingCycles;
            long totalItems;
            
            // Применяем фильтры, если они есть
            if (status != null && !status.isEmpty()) {
                boolean active = "active".equals(status);
                trainingCycles = trainingCycleService.findByActive(active, offset, pageSize);
                totalItems = trainingCycleService.countByActive(active);
            } else if (query != null && !query.isEmpty()) {
                trainingCycles = trainingCycleService.search(query, offset, pageSize);
                totalItems = trainingCycleService.countBySearch(query);
            } else {
                trainingCycles = trainingCycleService.findAll(offset, pageSize);
                totalItems = trainingCycleService.countTrainingCycles();
            }
            
            int totalPages = (int) Math.ceil((double) totalItems / pageSize);
            
            model.addAttribute("trainingCycles", trainingCycles);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", totalPages);
            
            return "admin/training-cycles/list";
        } catch (Exception e) {
            logger.error("Error listing training cycles", e);
            redirectAttributes.addFlashAttribute("error", "Произошла ошибка при загрузке списка тренировочных циклов: " + e.getMessage());
            return "redirect:/admin";
        }
    }
    
    /**
     * Show training cycle details
     */
    @GetMapping("/{id}")
    public String viewTrainingCycle(
            HttpServletRequest request,
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        if (!currentUser.hasRole("ADMIN")) {
            redirectAttributes.addFlashAttribute("error", "У вас нет доступа к управлению тренировочными циклами");
            return "redirect:/";
        }
        
        try {
            TrainingCycle cycle = trainingCycleService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Тренировочный цикл не найден: ID " + id));
            model.addAttribute("cycle", cycle);
            return "admin/training-cycles/view";
        } catch (Exception e) {
            logger.error("Ошибка при просмотре тренировочного цикла", e);
            redirectAttributes.addFlashAttribute("error", "Ошибка при просмотре тренировочного цикла: " + e.getMessage());
            return "redirect:/admin/training-cycles";
        }
    }
    
    /**
     * Show training cycle creation form
     */
    @GetMapping("/create")
    public String showCreateForm(HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        if (!currentUser.hasRole("ADMIN")) {
            redirectAttributes.addFlashAttribute("error", "У вас нет доступа к управлению тренировочными циклами");
            return "redirect:/";
        }
        
        model.addAttribute("trainingCycle", new TrainingCycle());
        return "admin/training-cycles/create";
    }
    
    /**
     * Process training cycle creation
     */
    @PostMapping("/create")
    public String createTrainingCycle(
            HttpServletRequest request,
            @Valid @ModelAttribute("trainingCycle") TrainingCycle trainingCycle,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        if (!currentUser.hasRole("ADMIN")) {
            redirectAttributes.addFlashAttribute("error", "У вас нет доступа к управлению тренировочными циклами");
            return "redirect:/";
        }
        
        if (bindingResult.hasErrors()) {
            return "admin/training-cycles/create";
        }
        
        try {
            TrainingCycle createdCycle = trainingCycleService.createTrainingCycle(trainingCycle);
            logger.info("Тренировочный цикл создан: {}", createdCycle.getId());
            redirectAttributes.addFlashAttribute("success", "Тренировочный цикл успешно создан");
            return "redirect:/admin/training-cycles/" + createdCycle.getId();
        } catch (Exception e) {
            logger.error("Ошибка при создании тренировочного цикла", e);
            model.addAttribute("error", "Ошибка при создании тренировочного цикла: " + e.getMessage());
            return "admin/training-cycles/create";
        }
    }
    
    /**
     * Show training cycle edit form
     */
    @GetMapping("/{id}/edit")
    public String showEditForm(
            HttpServletRequest request,
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        if (!currentUser.hasRole("ADMIN")) {
            redirectAttributes.addFlashAttribute("error", "У вас нет доступа к управлению тренировочными циклами");
            return "redirect:/";
        }
        
        try {
            TrainingCycle cycle = trainingCycleService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Тренировочный цикл не найден: ID " + id));
            model.addAttribute("trainingCycle", cycle);
            return "admin/training-cycles/edit";
        } catch (Exception e) {
            logger.error("Ошибка при загрузке формы редактирования", e);
            redirectAttributes.addFlashAttribute("error", "Ошибка при загрузке формы редактирования: " + e.getMessage());
            return "redirect:/admin/training-cycles";
        }
    }
    
    /**
     * Process training cycle update
     */
    @PostMapping("/{id}/edit")
    public String updateTrainingCycle(
            HttpServletRequest request,
            @PathVariable Long id,
            @Valid @ModelAttribute("trainingCycle") TrainingCycle trainingCycle,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        if (!currentUser.hasRole("ADMIN")) {
            redirectAttributes.addFlashAttribute("error", "У вас нет доступа к управлению тренировочными циклами");
            return "redirect:/";
        }
        
        if (bindingResult.hasErrors()) {
            return "admin/training-cycles/edit";
        }
        
        try {
            // Убедимся, что ID совпадает с path variable
            trainingCycle.setId(id);
            
            TrainingCycle updatedCycle = trainingCycleService.updateTrainingCycle(trainingCycle);
            logger.info("Тренировочный цикл обновлен: {}", updatedCycle.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Тренировочный цикл успешно обновлен");
            return "redirect:/admin/training-cycles/" + id;
        } catch (Exception e) {
            logger.error("Ошибка при обновлении тренировочного цикла", e);
            model.addAttribute("error", "Ошибка при обновлении тренировочного цикла: " + e.getMessage());
            return "admin/training-cycles/edit";
        }
    }
    
    /**
     * Activate training cycle
     */
    @PostMapping("/{id}/activate")
    public String activateTrainingCycle(
            HttpServletRequest request,
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        if (!currentUser.hasRole("ADMIN")) {
            redirectAttributes.addFlashAttribute("error", "У вас нет доступа к управлению тренировочными циклами");
            return "redirect:/";
        }
        
        try {
            boolean activated = trainingCycleService.activateTrainingCycle(id);
            if (activated) {
                logger.info("Тренировочный цикл активирован: {}", id);
                redirectAttributes.addFlashAttribute("successMessage", "Тренировочный цикл успешно активирован");
            } else {
                logger.warn("Не удалось активировать тренировочный цикл: {}", id);
                redirectAttributes.addFlashAttribute("errorMessage", "Не удалось активировать тренировочный цикл");
            }
        } catch (Exception e) {
            logger.error("Ошибка при активации тренировочного цикла", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при активации тренировочного цикла: " + e.getMessage());
        }
        
        return "redirect:/admin/training-cycles/" + id;
    }
    
    /**
     * Deactivate training cycle
     */
    @PostMapping("/{id}/deactivate")
    public String deactivateTrainingCycle(
            HttpServletRequest request,
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        if (!currentUser.hasRole("ADMIN")) {
            redirectAttributes.addFlashAttribute("error", "У вас нет доступа к управлению тренировочными циклами");
            return "redirect:/";
        }
        
        try {
            boolean deactivated = trainingCycleService.deactivateTrainingCycle(id);
            if (deactivated) {
                logger.info("Тренировочный цикл деактивирован: {}", id);
                redirectAttributes.addFlashAttribute("successMessage", "Тренировочный цикл успешно деактивирован");
            } else {
                logger.warn("Не удалось деактивировать тренировочный цикл: {}", id);
                redirectAttributes.addFlashAttribute("errorMessage", "Не удалось деактивировать тренировочный цикл");
            }
        } catch (Exception e) {
            logger.error("Ошибка при деактивации тренировочного цикла", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при деактивации тренировочного цикла: " + e.getMessage());
        }
        
        return "redirect:/admin/training-cycles/" + id;
    }
    
    /**
     * Delete training cycle
     */
    @PostMapping("/{id}/delete")
    public String deleteTrainingCycle(
            HttpServletRequest request,
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        if (!currentUser.hasRole("ADMIN")) {
            redirectAttributes.addFlashAttribute("error", "У вас нет доступа к управлению тренировочными циклами");
            return "redirect:/";
        }
        
        try {
            boolean deleted = trainingCycleService.deleteTrainingCycle(id);
            if (deleted) {
                logger.info("Тренировочный цикл удален: {}", id);
                redirectAttributes.addFlashAttribute("success", "Тренировочный цикл успешно удален");
            } else {
                logger.warn("Не удалось удалить тренировочный цикл: {}", id);
                redirectAttributes.addFlashAttribute("error", "Не удалось удалить тренировочный цикл");
            }
        } catch (Exception e) {
            logger.error("Ошибка при удалении тренировочного цикла", e);
            redirectAttributes.addFlashAttribute("error", "Ошибка при удалении тренировочного цикла: " + e.getMessage());
        }
        
        return "redirect:/admin/training-cycles";
    }
} 