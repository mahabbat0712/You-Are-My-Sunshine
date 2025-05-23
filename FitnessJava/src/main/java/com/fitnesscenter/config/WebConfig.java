package com.fitnesscenter.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.Locale;
import java.util.Arrays;

/**
 * Spring MVC configuration
 */
@Configuration
@EnableWebMvc
@ComponentScan(basePackages = {"com.fitnesscenter.controller", "com.fitnesscenter.service", "com.fitnesscenter.dao"})
public class WebConfig implements WebMvcConfigurer {
    
    private static final Logger logger = LogManager.getLogger(WebConfig.class);
    
    private final ApplicationContext applicationContext;
    
    @Autowired
    public WebConfig(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
    
    /**
     * Configure Thymeleaf template resolver
     */
    @Bean
    public SpringResourceTemplateResolver templateResolver() {
        SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
        templateResolver.setApplicationContext(applicationContext);
        templateResolver.setPrefix("/WEB-INF/templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCharacterEncoding("UTF-8");
        templateResolver.setCacheable(false); // Set to true in production
        templateResolver.setOrder(1);
        templateResolver.setCheckExistence(true);
        logger.info("Configured template resolver for /WEB-INF/templates/");
        return templateResolver;
    }
    
    /**
     * Additional template resolver for views directory
     */
    @Bean
    public SpringResourceTemplateResolver viewResolver() {
        SpringResourceTemplateResolver viewResolver = new SpringResourceTemplateResolver();
        viewResolver.setApplicationContext(applicationContext);
        viewResolver.setPrefix("/WEB-INF/views/");
        viewResolver.setSuffix(".html");
        viewResolver.setTemplateMode(TemplateMode.HTML);
        viewResolver.setCharacterEncoding("UTF-8");
        viewResolver.setCacheable(false);
        viewResolver.setOrder(2);
        viewResolver.setCheckExistence(true);
        logger.info("Configured template resolver for /WEB-INF/views/");
        return viewResolver;
    }
    
    /**
     * Configure Thymeleaf template engine
     */
    @Bean
    public SpringTemplateEngine templateEngine() {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.addTemplateResolver(templateResolver());
        templateEngine.addTemplateResolver(viewResolver());
        templateEngine.setEnableSpringELCompiler(true);
        
        // Add Java 8 time dialect for supporting #temporals helper
        templateEngine.addDialect(new Java8TimeDialect());
        logger.info("Configured template engine with Java8TimeDialect");
        
        return templateEngine;
    }
    
    /**
     * Configure view resolver
     */
    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        ThymeleafViewResolver resolver = new ThymeleafViewResolver();
        resolver.setTemplateEngine(templateEngine());
        resolver.setCharacterEncoding("UTF-8");
        registry.viewResolver(resolver);
        logger.info("Configured ThymeleafViewResolver");
    }
    
    /**
     * Configure resource handlers for static resources
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**")
                .addResourceLocations("/static/");
        
        registry.addResourceHandler("/css/**")
                .addResourceLocations("/css/");
                
        registry.addResourceHandler("/js/**")
                .addResourceLocations("/js/");
                
        registry.addResourceHandler("/images/**")
                .addResourceLocations("/images/");
        
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("/webjars/");
        
        logger.info("Configured resource handlers for static resources");
    }
    
    /**
     * Configure locale resolver
     */
    @Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver resolver = new CookieLocaleResolver();
        resolver.setDefaultLocale(Locale.ENGLISH); // Default locale
        resolver.setCookieName("language");
        resolver.setCookieMaxAge(3600); // Cookie expiry in seconds
        return resolver;
    }
    
    /**
     * Configure locale interceptor
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang"); // Request parameter to change language
        return interceptor;
    }
    
    /**
     * Add interceptors
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
    
    /**
     * Configure message source for i18n
     */
    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasenames("messages", "validation");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }
} 