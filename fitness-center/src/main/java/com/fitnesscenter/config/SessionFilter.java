package com.fitnesscenter.config;

import com.fitnesscenter.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Filter to maintain session continuity and log session information
 */
public class SessionFilter extends OncePerRequestFilter {
    private static final Logger logger = LogManager.getLogger(SessionFilter.class);
    
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        
        String requestURI = request.getRequestURI();
        HttpSession session = request.getSession(false);
        
        // Add session ID to response headers for debugging
        if (session != null) {
            String sessionId = session.getId();
            response.setHeader("X-Session-ID", sessionId);
            
            // Log session details for debugging
            User user = (User) session.getAttribute("user");
            if (user != null) {
                logger.debug("Request: {}, Session: {}, User: {}", 
                        requestURI, sessionId, user.getUsername());
                
                // Touch session to prevent timeout
                session.setAttribute("lastAccess", System.currentTimeMillis());
            } else {
                logger.debug("Request: {}, Session: {} (No user)", requestURI, sessionId);
            }
        } else {
            logger.debug("Request: {} (No session)", requestURI);
        }
        
        // Add secure flags to cookies
        response.setHeader("Set-Cookie", "JSESSIONID=" + 
                (session != null ? session.getId() : "") + 
                "; HttpOnly; SameSite=Strict");
        
        filterChain.doFilter(request, response);
    }
} 