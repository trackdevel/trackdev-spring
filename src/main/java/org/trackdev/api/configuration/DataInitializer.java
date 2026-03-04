package org.trackdev.api.configuration;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.trackdev.api.entity.User;
import org.trackdev.api.service.DemoDataSeeder;
import org.trackdev.api.service.UserService;

import java.util.Arrays;
import java.util.List;

/**
 * Initializes data on application startup.
 *
 * Behavior:
 * - Non-prod profiles: Seeds demo data for development/testing
 * - Prod profile: Runs Flyway migrations in stages (partial → seed → remaining),
 *   then ensures an admin user exists.
 *   Flyway is deferred via {@link org.trackdev.api.configuration.flywaydb.AfterMigrateCallback}
 *   so that JPA is available for data seeding between migration steps.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final Environment environment;
    private final ApplicationContext context;

    public DataInitializer(Environment environment, ApplicationContext context) {
        this.environment = environment;
        this.context = context;
    }

    @Override
    public void run(String... args) throws Exception {
        List<String> activeProfiles = Arrays.asList(environment.getActiveProfiles());
        boolean isProdProfile = activeProfiles.contains("prod");

        if (!isProdProfile) {
            // Development/test mode: seed demo data (Flyway disabled, Hibernate manages schema)
            logger.info("Initializing demo data for development environment...");
            context.getBean(DemoDataSeeder.class).seedDemoData();
            logger.info("Demo data initialization complete");
        } else {
            // Production mode: Flyway was deferred by AfterMigrateCallback (no-op strategy).
            // Now JPA is ready, so we can run migrations in stages with data seeding in between.
            logger.info("Running production initialization (Flyway + data seeding)...");
            ensureAdminUserExists();
        }
    }

    /**
     * Runs Flyway migrations in 3 stages:
     * 1. Migrate up to V13 (creates all tables needed for demo data)
     * 2. Seed demo data (requires JPA/EntityManager)
     * 3. Run remaining migrations (future migrations that may depend on seeded data)
     */
    private void runFlywayWithDataSeeding() {
        Flyway flyway = context.getBean(Flyway.class);

        // Step 1: Run migrations up to V13
        logger.info("Step 1: Running Flyway migrations up to V13...");
        Flyway partialFlyway = Flyway.configure()
            .configuration(flyway.getConfiguration())
            .target(MigrationVersion.fromVersion("13"))
            .load();
        partialFlyway.migrate();

        // Step 2: Seed demo data (JPA is available now)
        logger.info("Step 2: Seeding demo data...");
        context.getBean(DemoDataSeeder.class).seedDemoData();

        // Step 3: Run remaining migrations
        logger.info("Step 3: Running remaining Flyway migrations...");
        flyway.migrate();

        logger.info("Production initialization complete.");
    }

    private void ensureAdminUserExists() {
        UserService userService = context.getBean(UserService.class);
        PasswordEncoder passwordEncoder = context.getBean(PasswordEncoder.class);

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

            userService.addUserInternal(
                username,
                "System Administrator",
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
