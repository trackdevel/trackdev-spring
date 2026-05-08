package org.trackdev.api.configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class FirebaseConfiguration {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfiguration.class);

    private final TrackDevProperties properties;

    public FirebaseConfiguration(TrackDevProperties properties) {
        this.properties = properties;
    }

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        TrackDevProperties.Firebase cfg = properties.getFirebase();
        if (!cfg.isConfigured()) {
            return null;
        }

        Path path = Paths.get(cfg.getServiceAccountPath());
        if (!Files.isRegularFile(path)) {
            log.error("Firebase service account file not found at '{}' — FCM will be disabled", path);
            return null;
        }

        if (!FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.getInstance();
        }

        try (FileInputStream stream = new FileInputStream(path.toFile())) {
            GoogleCredentials credentials = GoogleCredentials.fromStream(stream);
            String projectId = credentials instanceof ServiceAccountCredentials sa
                    ? sa.getProjectId()
                    : null;

            FirebaseOptions.Builder optionsBuilder = FirebaseOptions.builder()
                    .setCredentials(credentials);
            if (projectId != null) {
                optionsBuilder.setProjectId(projectId);
            }

            FirebaseApp app = FirebaseApp.initializeApp(optionsBuilder.build());
            log.info("Firebase Admin SDK initialized from {} (project: {})",
                    path, projectId != null ? projectId : "<unknown>");
            return app;
        }
    }

    @Bean
    public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        if (firebaseApp == null) {
            return null;
        }
        return FirebaseMessaging.getInstance(firebaseApp);
    }

    @Bean
    public ApplicationListener<ApplicationReadyEvent> firebaseStartupWarning() {
        return event -> {
            if (!properties.getFirebase().isConfigured()) {
                log.warn("FIREBASE_SERVICE_ACCOUNT_JSON not set — FCM push notifications disabled");
            }
        };
    }
}
