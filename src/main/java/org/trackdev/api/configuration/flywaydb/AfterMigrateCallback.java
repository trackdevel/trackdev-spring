package org.trackdev.api.configuration.flywaydb;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Defers Flyway migration execution so that it can be run in stages
 * by {@link org.trackdev.api.configuration.DataInitializer}.
 *
 * This is needed because demo data seeding requires JPA (EntityManager),
 * which isn't available during Flyway initialization. By deferring,
 * we allow JPA to initialize first, then run: partial migration → seed → remaining migration.
 */
@Component
@Profile("migration")
public class AfterMigrateCallback implements FlywayMigrationStrategy {

    @Override
    public void migrate(Flyway flyway) {
        // No-op: migrations are executed by DataInitializer (CommandLineRunner)
        // after JPA EntityManager is available.
    }
}
