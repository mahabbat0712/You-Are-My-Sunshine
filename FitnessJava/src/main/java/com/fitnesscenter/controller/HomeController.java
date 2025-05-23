package com.fitnesscenter.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for the home page
 */
@Controller
public class HomeController {
    
    /**
     * Home page
     *
     * @param model Model
     * @return View name
     */
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "Fitness Center");
        return "home";
    }
} 