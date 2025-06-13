package com.fitnesscenter.controller;

import com.fitnesscenter.model.AccountType;
import com.fitnesscenter.model.DiscountRule;
import com.fitnesscenter.service.DiscountService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Controller for managing discounts and account types
 */
@Controller
@RequestMapping("/admin/discounts")
public class DiscountController {

    private static final Logger logger = LogManager.getLogger(DiscountController.class);
    private final DiscountService discountService;
    
    @Autowired
    public DiscountController(DiscountService discountService) {
        this.discountService = discountService;
        logger.info("DiscountController initialized");
    }
    
    @GetMapping
    public String listDiscounts(Model model) {
        logger.debug("Displaying discount rules list");
        List<DiscountRule> discounts = discountService.findAllDiscountRules();
        List<AccountType> accountTypes = discountService.findAllAccountTypes();
        
        model.addAttribute("discounts", discounts);
        model.addAttribute("accountTypes", accountTypes);
        model.addAttribute("newDiscount", new DiscountRule());
        
        return "admin/discounts/list";
    }
    
    @GetMapping("/{id}")
    public String viewDiscount(@PathVariable("id") Integer id, Model model, RedirectAttributes redirectAttributes) {
        logger.debug("Viewing discount rule with ID: {}", id);
        
        Optional<DiscountRule> discountOpt = discountService.findDiscountRuleById(id);
        if (!discountOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Discount rule not found");
            return "redirect:/admin/discounts";
        }
        
        model.addAttribute("discount", discountOpt.get());
        model.addAttribute("accountTypes", discountService.findAllAccountTypes());
        
        return "admin/discounts/view";
    }
    
    @GetMapping("/create")
    public String createDiscountForm(Model model) {
        logger.debug("Displaying create discount rule form");
        
        model.addAttribute("discount", new DiscountRule());
        model.addAttribute("accountTypes", discountService.findAllAccountTypes());
        
        return "admin/discounts/form";
    }
    
    @PostMapping("/create")
    public String createDiscount(@Valid @ModelAttribute("discount") DiscountRule discount, 
                                BindingResult result,
                                @RequestParam(value = "accountTypeId", required = false) Integer accountTypeId,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        logger.debug("Creating discount rule: {}, accountTypeId: {}", discount, accountTypeId);
        
        if (result.hasErrors()) {
            model.addAttribute("accountTypes", discountService.findAllAccountTypes());
            return "admin/discounts/form";
        }
        
        // Set account type if ID is provided
        if (accountTypeId != null) {
            discountService.findAccountTypeById(accountTypeId).ifPresent(discount::setAccountType);
        }
        
        // Save the discount
        try {
            DiscountRule savedDiscount = discountService.saveDiscountRule(discount);
            logger.debug("Discount rule created successfully: {}", savedDiscount);
            redirectAttributes.addFlashAttribute("success", "Discount rule created successfully");
            return "redirect:/admin/discounts";
        } catch (Exception e) {
            logger.error("Error creating discount rule", e);
            model.addAttribute("error", "Failed to create discount rule: " + e.getMessage());
            model.addAttribute("accountTypes", discountService.findAllAccountTypes());
            return "admin/discounts/form";
        }
    }
    
    @GetMapping("/edit/{id}")
    public String editDiscountForm(@PathVariable("id") Integer id, Model model, RedirectAttributes redirectAttributes) {
        logger.debug("Displaying edit form for discount rule ID: {}", id);
        
        Optional<DiscountRule> discountOpt = discountService.findDiscountRuleById(id);
        if (!discountOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Discount rule not found");
            return "redirect:/admin/discounts";
        }
        
        model.addAttribute("discount", discountOpt.get());
        model.addAttribute("accountTypes", discountService.findAllAccountTypes());
        
        return "admin/discounts/form";
    }
    
    @PostMapping("/edit/{id}")
    public String updateDiscount(@PathVariable("id") Integer id,
                                @Valid @ModelAttribute("discount") DiscountRule discount,
                                BindingResult result,
                                @RequestParam(value = "accountTypeId", required = false) Integer accountTypeId,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        logger.debug("Updating discount rule ID: {}, data: {}, accountTypeId: {}", id, discount, accountTypeId);
        
        discount.setId(id); // Ensure ID is set
        
        if (result.hasErrors()) {
            model.addAttribute("accountTypes", discountService.findAllAccountTypes());
            return "admin/discounts/form";
        }
        
        // Set account type if ID is provided
        if (accountTypeId != null) {
            discountService.findAccountTypeById(accountTypeId).ifPresent(discount::setAccountType);
        }
        
        // Save the discount
        try {
            DiscountRule savedDiscount = discountService.saveDiscountRule(discount);
            logger.debug("Discount rule updated successfully: {}", savedDiscount);
            redirectAttributes.addFlashAttribute("success", "Discount rule updated successfully");
            return "redirect:/admin/discounts";
        } catch (Exception e) {
            logger.error("Error updating discount rule", e);
            model.addAttribute("error", "Failed to update discount rule: " + e.getMessage());
            model.addAttribute("accountTypes", discountService.findAllAccountTypes());
            return "admin/discounts/form";
        }
    }
    
    @PostMapping("/delete/{id}")
    public String deleteDiscount(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        logger.debug("Deleting discount rule with ID: {}", id);
        
        try {
            boolean deleted = discountService.deleteDiscountRule(id);
            if (deleted) {
                logger.debug("Discount rule deleted successfully, ID: {}", id);
                redirectAttributes.addFlashAttribute("success", "Discount rule deleted successfully");
            } else {
                logger.warn("Failed to delete discount rule, ID: {}", id);
                redirectAttributes.addFlashAttribute("error", "Failed to delete discount rule");
            }
        } catch (Exception e) {
            logger.error("Error deleting discount rule", e);
            redirectAttributes.addFlashAttribute("error", "Error deleting discount rule: " + e.getMessage());
        }
        
        return "redirect:/admin/discounts";
    }
    
    @PostMapping("/{id}/toggle-active")
    public String toggleActive(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        logger.debug("Toggling active status for discount rule ID: {}", id);
        
        try {
            Optional<DiscountRule> discountOpt = discountService.findDiscountRuleById(id);
            if (!discountOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Discount rule not found");
                return "redirect:/admin/discounts";
            }
            
            DiscountRule discount = discountOpt.get();
            boolean newStatus = !discount.isActive();
            
            boolean updated = discountService.setDiscountRuleActive(id, newStatus);
            if (updated) {
                String statusText = newStatus ? "activated" : "deactivated";
                logger.debug("Discount rule {} successfully, ID: {}", statusText, id);
                redirectAttributes.addFlashAttribute("success", "Discount rule " + statusText + " successfully");
            } else {
                logger.warn("Failed to update discount rule status, ID: {}", id);
                redirectAttributes.addFlashAttribute("error", "Failed to update discount rule status");
            }
        } catch (Exception e) {
            logger.error("Error updating discount rule status", e);
            redirectAttributes.addFlashAttribute("error", "Error updating discount rule status: " + e.getMessage());
        }
        
        return "redirect:/admin/discounts";
    }
    
    @GetMapping("/api/calculate")
    @ResponseBody
    public ResponseEntity<?> calculateDiscount(@RequestParam("clientId") Long clientId) {
        logger.debug("API request to calculate discount for client ID: {}", clientId);
        
        try {
            BigDecimal discount = discountService.calculateDiscountForClient(clientId);
            return ResponseEntity.ok().body(discount);
        } catch (Exception e) {
            logger.error("Error calculating discount", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error calculating discount: " + e.getMessage());
        }
    }
    
    @GetMapping("/api/account-types")
    @ResponseBody
    public ResponseEntity<?> getAccountTypes() {
        logger.debug("API request to get all account types");
        
        try {
            List<AccountType> accountTypes = discountService.findAllAccountTypes();
            return ResponseEntity.ok().body(accountTypes);
        } catch (Exception e) {
            logger.error("Error fetching account types", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching account types: " + e.getMessage());
        }
    }
}