package org.trackdev.api.configuration;

import io.minio.MinioClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(MinioConfiguration.class);

    @Bean
    @ConditionalOnProperty(prefix = "todospring.minio", name = {"url", "access-key", "secret-key", "bucket"})
    public MinioClient minioClient(MinioProperties minioProperties) {
        logger.info("Initializing MinIO client with URL: {}", minioProperties.getUrl());
        
        try {
            return MinioClient.builder()
                    .endpoint(minioProperties.getUrl())
                    .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                    .build();
        } catch (Exception e) {
            logger.error("Failed to initialize MinIO client", e);
            throw new IllegalStateException("Could not initialize MinIO client", e);
        }
    }
}
