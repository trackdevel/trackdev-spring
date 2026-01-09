package org.trackdev.api.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.trackdev.api.service.DemoDataSeeder;

/**
 * Initializes demo data on application startup.
 * Separated from configuration to follow Single Responsibility Principle.
 * 
 * Only runs when profile is NOT 'prod' (runs in dev, test, default, etc.)
 */
@Component
@Profile("!prod")
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    @Lazy
    private DemoDataSeeder demoDataSeeder;

    @Override
    public void run(String... args) throws Exception {
        logger.info("Initializing demo data...");
        demoDataSeeder.seedDemoData();
        logger.info("Demo data initialization complete");
    }
}
