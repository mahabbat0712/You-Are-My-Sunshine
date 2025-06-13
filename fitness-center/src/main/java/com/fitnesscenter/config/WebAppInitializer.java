package com.fitnesscenter.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import javax.servlet.Filter;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * Web application initializer (replacement for web.xml)
 */
public class WebAppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {
    
    private static final Logger logger = LogManager.getLogger(WebAppInitializer.class);
    
    @Override
    protected Class<?>[] getRootConfigClasses() {
        logger.info("Loading root configuration classes: AppConfig");
        return new Class<?>[] { AppConfig.class };
    }
    
    @Override
    protected Class<?>[] getServletConfigClasses() {
        logger.info("Loading servlet configuration classes: WebConfig");
        return new Class<?>[] { WebConfig.class };
    }
    
    @Override
    protected String[] getServletMappings() {
        logger.info("Configuring DispatcherServlet mappings: /");
        return new String[] { "/" };
    }
    
    @Override
    protected Filter[] getServletFilters() {
        // Add character encoding filter
        CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
        characterEncodingFilter.setEncoding("UTF-8");
        characterEncodingFilter.setForceEncoding(true);
        
        // Add session filter
        SessionFilter sessionFilter = new SessionFilter();
        
        logger.info("Configuring servlet filters: CharacterEncodingFilter, SessionFilter");
        return new Filter[] { characterEncodingFilter, sessionFilter };
    }
    
    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        logger.info("Starting WebAppInitializer");
        super.onStartup(servletContext);
        logger.info("WebAppInitializer started successfully");
    }
} 