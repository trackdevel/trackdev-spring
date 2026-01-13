package org.trackdev.api.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.trackdev.api.configuration.UserType;
import org.trackdev.api.entity.User;
import org.trackdev.api.service.DemoDataSeeder;
import org.trackdev.api.service.UserService;

import java.util.Arrays;
import java.util.List;

/**
 * Initializes data on application startup.
 * Separated from configuration to follow Single Responsibility Principle.
 * 
 * Behavior:
 * - Non-prod profiles: Seeds demo data for development/testing
 * - Prod profile: Creates initial admin user if none exists
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private Environment environment;

    @Autowired
    @Lazy
    private DemoDataSeeder demoDataSeeder;

    @Autowired
    private UserService userService;

    @Autowired
    private TrackDevProperties trackDevProperties;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        List<String> activeProfiles = Arrays.asList(environment.getActiveProfiles());
        boolean isProdProfile = activeProfiles.contains("prod");

        if (!isProdProfile) {
            // Development/test mode: seed demo data
            logger.info("Initializing demo data for development environment...");
            demoDataSeeder.seedDemoData();
            logger.info("Demo data initialization complete");
        } else {
            // Production mode: ensure admin user exists
            logger.info("Running in production mode - checking for admin user...");
            ensureAdminUserExists();
        }
    }

    private void ensureAdminUserExists() {
        // Check if any admin user exists
        List<User> adminUsers = userService.getUsersByType(UserType.ADMIN);
        
        if (adminUsers.isEmpty()) {
            logger.info("No admin user found. Creating initial admin user...");
            
            String username = environment.getProperty("ADMIN_USERNAME");
            String email = environment.getProperty("ADMIN_EMAIL");
            String password = environment.getProperty("ADMIN_PASSWORD");
            
            if (username == null || username.isBlank() || 
                email == null || email.isBlank() || 
                password == null || password.isBlank()) {
                logger.error("Admin credentials not configured! Set ADMIN_USERNAME, ADMIN_EMAIL and ADMIN_PASSWORD environment variables");
                throw new IllegalStateException("Admin credentials must be configured in production");
            }
            
            User adminUser = userService.addUserInternal(
                username,
                email,
                passwordEncoder.encode(password),
                List.of(UserType.ADMIN)
            );
            
            logger.info("Admin user created successfully: {}", username);
        } else {
            logger.info("Admin user already exists. Skipping creation.");
        }
    }
}
