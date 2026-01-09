package org.trackdev.api.service;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.trackdev.api.configuration.MinioProperties;

import java.util.Optional;

/**
 * Service for MinIO file storage operations.
 * Only active when MinIO is properly configured.
 */
@Service
@ConditionalOnBean(MinioClient.class)
public class MinioService {

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MinioProperties minioProperties;

    public MinioClient getClient() {
        return minioClient;
    }

    public String getBucket() {
        return minioProperties.getBucket();
    }

    public Optional<MinioClient> getClientIfAvailable() {
        return Optional.ofNullable(minioClient);
    }

    public boolean isAvailable() {
        return minioClient != null && minioProperties.getBucket() != null 
                && !minioProperties.getBucket().isEmpty();
    }
}
