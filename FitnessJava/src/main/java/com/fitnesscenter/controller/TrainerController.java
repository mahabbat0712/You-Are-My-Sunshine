package com.fitnesscenter.controller;

import com.fitnesscenter.model.User;
import com.fitnesscenter.service.TrainerService;
import com.fitnesscenter.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * Controller for trainer management
 */
@Controller
@RequestMapping("/trainers")
public class TrainerController {
    private static final Logger logger = LogManager.getLogger(TrainerController.class);
    
    private final UserService userService;
    private final TrainerService trainerService;
    
    @Autowired
    public TrainerController(UserService userService, TrainerService trainerService) {
        this.userService = userService;
        this.trainerService = trainerService;
        logger.info("TrainerController constructed");
    }
    
    @PostConstruct
    public void init() {
        logger.info("TrainerController initialized, mapping path: /trainers");
    }
    
    /**
     * List all trainers
     *
     * @param model Model
     * @param page Page number (optional)
     * @return View name
     */
    @GetMapping
    public String listTrainers(Model model, @RequestParam(defaultValue = "1") int page) {
        logger.info("Handling GET request for /trainers with page={}", page);
        int pageSize = 8;
        List<User> trainers = trainerService.findAllTrainers(page, pageSize);
        int totalTrainers = trainerService.countTrainers();
        
        model.addAttribute("trainers", trainers);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalTrainers", totalTrainers);
        model.addAttribute("totalPages", (int) Math.ceil((double) totalTrainers / pageSize));
        
        logger.info("Returning view: trainers/list");
        return "trainers/list";
    }
    
    /**
     * Show trainer details
     *
     * @param id Trainer ID
     * @param model Model
     * @return View name
     */
    @GetMapping("/{id}")
    public String viewTrainer(@PathVariable Long id, Model model) {
        logger.info("Handling GET request for /trainers/{}", id);
        User trainer = trainerService.findTrainerById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid trainer ID: " + id));
        
        model.addAttribute("trainer", trainer);
        return "trainers/view";
    }
} 