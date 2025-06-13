package com.fitnesscenter.service;

import com.fitnesscenter.dao.UserDao;
import com.fitnesscenter.model.User;
import com.fitnesscenter.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserDao userDao;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Create a simple test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
    }

    @Test
    @DisplayName("Test 1: Find user by username - user exists")
    void testFindUserByUsername_UserExists() {
        // Arrange
        when(userDao.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = userService.findByUsername("testuser");

        // Assert
        assertTrue(result.isPresent(), "User should be found");
        assertEquals("testuser", result.get().getUsername(), "Username should match");
        verify(userDao, times(1)).findByUsername("testuser");
    }

    @Test
    @DisplayName("Test 2: Find user by username - user does not exist")
    void testFindUserByUsername_UserDoesNotExist() {
        // Arrange
        when(userDao.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.findByUsername("nonexistent");

        // Assert
        assertFalse(result.isPresent(), "User should not be found");
        verify(userDao, times(1)).findByUsername("nonexistent");
    }

    @Test
    @DisplayName("Test 3: Find user by email - user exists")
    void testFindUserByEmail_UserExists() {
        // Arrange
        when(userDao.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = userService.findByEmail("test@example.com");

        // Assert
        assertTrue(result.isPresent(), "User should be found");
        assertEquals("test@example.com", result.get().getEmail(), "Email should match");
        verify(userDao, times(1)).findByEmail("test@example.com");
    }
} 