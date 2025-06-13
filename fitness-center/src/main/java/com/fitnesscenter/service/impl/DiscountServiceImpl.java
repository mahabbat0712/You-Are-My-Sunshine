package com.fitnesscenter.service.impl;

import com.fitnesscenter.dao.AccountTypeDao;
import com.fitnesscenter.dao.ClientProfileDao;
import com.fitnesscenter.dao.DiscountRuleDao;
import com.fitnesscenter.model.AccountType;
import com.fitnesscenter.model.ClientProfile;
import com.fitnesscenter.model.DiscountRule;
import com.fitnesscenter.service.DiscountService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of DiscountService interface
 */
@Service
public class DiscountServiceImpl implements DiscountService {

    private static final Logger logger = LogManager.getLogger(DiscountServiceImpl.class);
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    
    private final AccountTypeDao accountTypeDao;
    private final DiscountRuleDao discountRuleDao;
    private final ClientProfileDao clientProfileDao;
    
    @Autowired
    public DiscountServiceImpl(AccountTypeDao accountTypeDao, 
                              DiscountRuleDao discountRuleDao,
                              ClientProfileDao clientProfileDao) {
        this.accountTypeDao = accountTypeDao;
        this.discountRuleDao = discountRuleDao;
        this.clientProfileDao = clientProfileDao;
        logger.info("DiscountServiceImpl initialized");
    }
    
    @Override
    public List<AccountType> findAllAccountTypes() {
        logger.debug("Finding all account types");
        return accountTypeDao.findAll();
    }
    
    @Override
    public Optional<AccountType> findAccountTypeById(Integer id) {
        logger.debug("Finding account type by ID: {}", id);
        return accountTypeDao.findById(id);
    }
    
    @Override
    public Optional<AccountType> findAccountTypeByName(String name) {
        logger.debug("Finding account type by name: {}", name);
        return accountTypeDao.findByName(name);
    }
    
    @Override
    public List<DiscountRule> findAllDiscountRules() {
        logger.debug("Finding all discount rules");
        return discountRuleDao.findAll();
    }
    
    @Override
    public Optional<DiscountRule> findDiscountRuleById(Integer id) {
        logger.debug("Finding discount rule by ID: {}", id);
        return discountRuleDao.findById(id);
    }
    
    @Override
    public List<DiscountRule> findDiscountRulesByAccountType(Integer accountTypeId) {
        logger.debug("Finding discount rules by account type ID: {}", accountTypeId);
        return discountRuleDao.findByAccountType(accountTypeId);
    }
    
    @Override
    public List<DiscountRule> findActiveDiscountRulesByAccountType(Integer accountTypeId) {
        logger.debug("Finding active discount rules by account type ID: {}", accountTypeId);
        return discountRuleDao.findActiveByAccountType(accountTypeId);
    }
    
    @Override
    public DiscountRule saveDiscountRule(DiscountRule discountRule) {
        logger.debug("Saving discount rule: {}", discountRule);
        return discountRuleDao.save(discountRule);
    }
    
    @Override
    public boolean deleteDiscountRule(Integer id) {
        logger.debug("Deleting discount rule by ID: {}", id);
        return discountRuleDao.deleteById(id);
    }
    
    @Override
    public boolean setDiscountRuleActive(Integer id, boolean active) {
        logger.debug("Setting discount rule active status. ID: {}, active: {}", id, active);
        return discountRuleDao.setActive(id, active);
    }
    
    @Override
    public BigDecimal calculateDiscountForClient(Long clientId) {
        logger.debug("Calculating discount for client ID: {}", clientId);
        
        Optional<ClientProfile> clientProfileOpt = clientProfileDao.findByUserId(clientId);
        if (!clientProfileOpt.isPresent()) {
            logger.warn("Client profile not found for user ID: {}", clientId);
            return BigDecimal.ZERO;
        }
        
        ClientProfile clientProfile = clientProfileOpt.get();
        Integer accountTypeId = clientProfile.getAccountType().getId();
        Integer completedCycles = clientProfile.getCompletedCycles();
        
        DiscountRule bestDiscount = discountRuleDao.findBestDiscount(accountTypeId, completedCycles);
        if (bestDiscount == null) {
            logger.debug("No discount rule applies for client ID: {}", clientId);
            return BigDecimal.ZERO;
        }
        
        logger.debug("Applied discount rule: {} with percentage: {} for client ID: {}", 
                bestDiscount.getName(), bestDiscount.getDiscountPercentage(), clientId);
                
        return bestDiscount.getDiscountPercentage();
    }
    
    @Override
    public BigDecimal[] applyDiscount(BigDecimal originalPrice, Long clientId) {
        logger.debug("Applying discount to price {} for client ID: {}", originalPrice, clientId);
        
        if (originalPrice == null || originalPrice.compareTo(BigDecimal.ZERO) <= 0) {
            logger.warn("Invalid original price: {}", originalPrice);
            return new BigDecimal[]{BigDecimal.ZERO, originalPrice};
        }
        
        BigDecimal discountPercentage = calculateDiscountForClient(clientId);
        
        if (discountPercentage.compareTo(BigDecimal.ZERO) == 0) {
            logger.debug("No discount applicable for client ID: {}", clientId);
            return new BigDecimal[]{BigDecimal.ZERO, originalPrice};
        }
        
        BigDecimal discount = originalPrice.multiply(discountPercentage)
                .divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
                
        BigDecimal finalPrice = originalPrice.subtract(discount).setScale(2, RoundingMode.HALF_UP);
        
        logger.debug("Applied discount of {}% ({}). Original price: {}, Final price: {}", 
                discountPercentage, discount, originalPrice, finalPrice);
                
        return new BigDecimal[]{discountPercentage, finalPrice};
    }
    
    @Override
    public boolean updateClientAccountType(Long userId, Integer accountTypeId) {
        logger.debug("Updating client account type. User ID: {}, Account type ID: {}", userId, accountTypeId);
        
        // Verify account type exists
        if (!accountTypeDao.findById(accountTypeId).isPresent()) {
            logger.warn("Account type with ID: {} not found", accountTypeId);
            return false;
        }
        
        return clientProfileDao.updateAccountType(userId, accountTypeId);
    }
} 