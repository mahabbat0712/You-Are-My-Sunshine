package com.fitnesscenter.service;

import com.fitnesscenter.dao.AccountTypeDao;
import com.fitnesscenter.dao.ClientProfileDao;
import com.fitnesscenter.dao.DiscountRuleDao;
import com.fitnesscenter.model.AccountType;
import com.fitnesscenter.model.ClientProfile;
import com.fitnesscenter.model.DiscountRule;
import com.fitnesscenter.model.User;
import com.fitnesscenter.service.impl.DiscountServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DiscountServiceTest {

    @Mock
    private AccountTypeDao accountTypeDao;
    
    @Mock
    private DiscountRuleDao discountRuleDao;
    
    @Mock
    private ClientProfileDao clientProfileDao;
    
    @InjectMocks
    private DiscountServiceImpl discountService;
    
    private AccountType regularAccountType;
    private AccountType corporateAccountType;
    private DiscountRule regularDiscount;
    private DiscountRule corporateDiscount;
    private User user;
    private ClientProfile clientProfile;
    
    @BeforeEach
    void setUp() {
        // Setup test data
        regularAccountType = new AccountType(1, "REGULAR", "Regular account");
        corporateAccountType = new AccountType(2, "CORPORATE", "Corporate account");
        
        regularDiscount = new DiscountRule();
        regularDiscount.setId(1);
        regularDiscount.setName("Regular Loyalty");
        regularDiscount.setAccountType(regularAccountType);
        regularDiscount.setMinCompletedCycles(3);
        regularDiscount.setDiscountPercentage(new BigDecimal("5.00"));
        regularDiscount.setActive(true);
        
        corporateDiscount = new DiscountRule();
        corporateDiscount.setId(2);
        corporateDiscount.setName("Corporate Base");
        corporateDiscount.setAccountType(corporateAccountType);
        corporateDiscount.setMinCompletedCycles(0);
        corporateDiscount.setDiscountPercentage(new BigDecimal("10.00"));
        corporateDiscount.setActive(true);
        
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        
        clientProfile = new ClientProfile();
        clientProfile.setUser(user);
        clientProfile.setAccountType(regularAccountType);
        clientProfile.setCompletedCycles(5); // Eligible for regular discount
    }
    
    @Test
    @DisplayName("Find all account types")
    void testFindAllAccountTypes() {
        // Arrange
        List<AccountType> accountTypes = Arrays.asList(regularAccountType, corporateAccountType);
        when(accountTypeDao.findAll()).thenReturn(accountTypes);
        
        // Act
        List<AccountType> result = discountService.findAllAccountTypes();
        
        // Assert
        assertEquals(2, result.size());
        assertEquals(regularAccountType.getId(), result.get(0).getId());
        assertEquals(corporateAccountType.getId(), result.get(1).getId());
        verify(accountTypeDao, times(1)).findAll();
    }
    
    @Test
    @DisplayName("Find account type by id - exists")
    void testFindAccountTypeById_Exists() {
        // Arrange
        when(accountTypeDao.findById(1)).thenReturn(Optional.of(regularAccountType));
        
        // Act
        Optional<AccountType> result = discountService.findAccountTypeById(1);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(regularAccountType.getId(), result.get().getId());
        verify(accountTypeDao, times(1)).findById(1);
    }
    
    @Test
    @DisplayName("Find account type by id - not exists")
    void testFindAccountTypeById_NotExists() {
        // Arrange
        when(accountTypeDao.findById(999)).thenReturn(Optional.empty());
        
        // Act
        Optional<AccountType> result = discountService.findAccountTypeById(999);
        
        // Assert
        assertFalse(result.isPresent());
        verify(accountTypeDao, times(1)).findById(999);
    }
    
    @Test
    @DisplayName("Calculate discount for client with eligible discount")
    void testCalculateDiscountForClient_WithEligibleDiscount() {
        // Arrange
        when(clientProfileDao.findByUserId(1L)).thenReturn(Optional.of(clientProfile));
        when(discountRuleDao.findBestDiscount(anyInt(), anyInt())).thenReturn(regularDiscount);
        
        // Act
        BigDecimal discount = discountService.calculateDiscountForClient(1L);
        
        // Assert
        assertEquals(new BigDecimal("5.00"), discount);
        verify(clientProfileDao, times(1)).findByUserId(1L);
        verify(discountRuleDao, times(1)).findBestDiscount(
                clientProfile.getAccountType().getId(), 
                clientProfile.getCompletedCycles());
    }
    
    @Test
    @DisplayName("Calculate discount for client with no eligible discount")
    void testCalculateDiscountForClient_NoEligibleDiscount() {
        // Arrange
        when(clientProfileDao.findByUserId(1L)).thenReturn(Optional.of(clientProfile));
        when(discountRuleDao.findBestDiscount(anyInt(), anyInt())).thenReturn(null);
        
        // Act
        BigDecimal discount = discountService.calculateDiscountForClient(1L);
        
        // Assert
        assertEquals(BigDecimal.ZERO, discount);
        verify(clientProfileDao, times(1)).findByUserId(1L);
        verify(discountRuleDao, times(1)).findBestDiscount(
                clientProfile.getAccountType().getId(), 
                clientProfile.getCompletedCycles());
    }
    
    @Test
    @DisplayName("Calculate discount for nonexistent client")
    void testCalculateDiscountForClient_NonexistentClient() {
        // Arrange
        when(clientProfileDao.findByUserId(999L)).thenReturn(Optional.empty());
        
        // Act
        BigDecimal discount = discountService.calculateDiscountForClient(999L);
        
        // Assert
        assertEquals(BigDecimal.ZERO, discount);
        verify(clientProfileDao, times(1)).findByUserId(999L);
        verify(discountRuleDao, never()).findBestDiscount(anyInt(), anyInt());
    }
    
    @Test
    @DisplayName("Apply discount to price")
    void testApplyDiscount() {
        // Arrange
        BigDecimal originalPrice = new BigDecimal("100.00");
        when(clientProfileDao.findByUserId(1L)).thenReturn(Optional.of(clientProfile));
        when(discountRuleDao.findBestDiscount(anyInt(), anyInt())).thenReturn(regularDiscount);
        
        // Act
        BigDecimal[] result = discountService.applyDiscount(originalPrice, 1L);
        
        // Assert
        assertEquals(2, result.length);
        assertEquals(new BigDecimal("5.00"), result[0]); // discount percentage
        assertEquals(new BigDecimal("95.00"), result[1]); // final price
    }
    
    @Test
    @DisplayName("Apply discount - zero price")
    void testApplyDiscount_ZeroPrice() {
        // Arrange
        BigDecimal originalPrice = BigDecimal.ZERO;
        
        // Act
        BigDecimal[] result = discountService.applyDiscount(originalPrice, 1L);
        
        // Assert
        assertEquals(2, result.length);
        assertEquals(BigDecimal.ZERO, result[0]); // no discount
        assertEquals(BigDecimal.ZERO, result[1]); // zero price remains
    }
    
    @Test
    @DisplayName("Save discount rule")
    void testSaveDiscountRule() {
        // Arrange
        when(discountRuleDao.save(any(DiscountRule.class))).thenReturn(regularDiscount);
        
        // Act
        DiscountRule result = discountService.saveDiscountRule(regularDiscount);
        
        // Assert
        assertNotNull(result);
        assertEquals(regularDiscount.getId(), result.getId());
        verify(discountRuleDao, times(1)).save(regularDiscount);
    }
    
    @Test
    @DisplayName("Update client account type - success")
    void testUpdateClientAccountType_Success() {
        // Arrange
        when(accountTypeDao.findById(2)).thenReturn(Optional.of(corporateAccountType));
        when(clientProfileDao.updateAccountType(1L, 2)).thenReturn(true);
        
        // Act
        boolean result = discountService.updateClientAccountType(1L, 2);
        
        // Assert
        assertTrue(result);
        verify(accountTypeDao, times(1)).findById(2);
        verify(clientProfileDao, times(1)).updateAccountType(1L, 2);
    }
    
    @Test
    @DisplayName("Update client account type - nonexistent account type")
    void testUpdateClientAccountType_NonexistentAccountType() {
        // Arrange
        when(accountTypeDao.findById(999)).thenReturn(Optional.empty());
        
        // Act
        boolean result = discountService.updateClientAccountType(1L, 999);
        
        // Assert
        assertFalse(result);
        verify(accountTypeDao, times(1)).findById(999);
        verify(clientProfileDao, never()).updateAccountType(anyLong(), anyInt());
    }
} 