package com.fitnesscenter.service;

import com.fitnesscenter.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Service for managing user sessions
 */
@Service
public class SessionService {
    private static final Logger logger = LogManager.getLogger(SessionService.class);
    
    /**
     * Default session timeout (8 hours)
     */
    private static final int DEFAULT_SESSION_TIMEOUT = 8 * 60 * 60; // 8 hours in seconds
    
    /**
     * Create a new session for a user
     *
     * @param request HTTP request
     * @param user User to store in session
     * @return Created session
     */
    public HttpSession createSession(HttpServletRequest request, User user) {
        // Invalidate any existing session
        HttpSession existingSession = request.getSession(false);
        if (existingSession != null) {
            logger.info("Invalidating existing session: {}", existingSession.getId());
            existingSession.invalidate();
        }
        
        // Create new session with longer timeout
        HttpSession session = request.getSession(true);
        session.setMaxInactiveInterval(DEFAULT_SESSION_TIMEOUT);
        
        // Store user and attributes
        session.setAttribute("user", user);
        session.setAttribute("userId", user.getId());
        session.setAttribute("username", user.getUsername());
        
        // Store role flags for easy access
        boolean isAdmin = user.hasRole("ADMIN");
        boolean isTrainer = user.hasRole("TRAINER");
        boolean isClient = user.hasRole("CLIENT");
        
        session.setAttribute("isAdmin", isAdmin);
        session.setAttribute("isTrainer", isTrainer);
        session.setAttribute("isClient", isClient);
        
        logger.info("Created new session: {}, user: {}, roles: {}", 
                session.getId(), user.getUsername(), user.getRoles());
        
        return session;
    }
    
    /**
     * Get current user from session
     *
     * @param request HTTP request
     * @return Current user or null if not logged in
     */
    public User getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                logger.warn("User not found in session {}, but session exists. Session attributes: {}", 
                    session.getId(), getSessionAttributeNames(session));
            } else {
                logger.debug("Retrieved user from session {}: {}, roles: {}", 
                        session.getId(), user.getUsername(), user.getRoles());
                
                // Touch session to prevent timeout
                session.setAttribute("lastAccess", System.currentTimeMillis());
            }
            return user;
        }
        logger.debug("No active session found when retrieving current user for URI: {}", request.getRequestURI());
        return null;
    }
    
    /**
     * Helper method to list all session attribute names for debugging
     */
    private String getSessionAttributeNames(HttpSession session) {
        StringBuilder sb = new StringBuilder();
        java.util.Enumeration<String> attributeNames = session.getAttributeNames();
        
        while (attributeNames.hasMoreElements()) {
            String name = attributeNames.nextElement();
            sb.append(name).append(", ");
        }
        
        return sb.toString();
    }
    
    /**
     * Check if user has a specific role
     *
     * @param request HTTP request
     * @param role Role name
     * @return True if user has role
     */
    public boolean hasRole(HttpServletRequest request, String role) {
        User user = getCurrentUser(request);
        if (user != null) {
            boolean hasRole = user.hasRole(role);
            if (!hasRole) {
                logger.warn("User {} does not have required role: {}", user.getUsername(), role);
            }
            return hasRole;
        }
        return false;
    }
    
    /**
     * Invalidate current session
     *
     * @param request HTTP request
     */
    public void invalidateSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            String username = (String) session.getAttribute("username");
            String sessionId = session.getId();
            logger.info("Invalidating session: {}, user: {}", sessionId, username);
            session.invalidate();
        }
    }
} 