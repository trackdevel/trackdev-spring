package org.trackdev.api.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Logs TrackDev configuration properties at application startup.
 * Only active in 'dev' profile.
 */
@Component
@Profile("dev")
public class TrackDevPropertiesLogger {

    private static final Logger log = LoggerFactory.getLogger(TrackDevPropertiesLogger.class);

    private final TrackDevProperties trackDevProperties;

    public TrackDevPropertiesLogger(TrackDevProperties trackDevProperties) {
        this.trackDevProperties = trackDevProperties;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void logPropertiesOnStartup() {
        log.info("\n" + "=".repeat(80));
        log.info("TrackDev Configuration Properties (dev profile):");
        log.info("=".repeat(80));
        log.info(trackDevProperties.toString());
        log.info("=".repeat(80));
    }
}
