package com.fitnesscenter.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.PostConstruct;

/**
 * Controller for About page
 */
@Controller
@RequestMapping("/about")
public class AboutController {
    private static final Logger logger = LogManager.getLogger(AboutController.class);
    
    @PostConstruct
    public void init() {
        logger.info("AboutController initialized, mapping path: /about");
    }
    
    /**
     * Show about page
     *
     * @param model Model
     * @return View name
     */
    @GetMapping
    public String about(Model model) {
        logger.info("Handling GET request for /about");
        model.addAttribute("title", "About Us - Fitness Center");
        logger.info("Returning view: about");
        return "about";
    }
} 