package org.trackdev.api.service;

import io.minio.MinioClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;

@Service
public class Global implements CommandLineRunner {
    public static final DateTimeFormatter AppDateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy - HH:mm:ss z");

    public static final String SIMPLE_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    public static final String SIMPLE_LOCALDATE_FORMAT = "yyyy-MM-dd";

    public static final SimpleDateFormat dateFormat = new SimpleDateFormat(SIMPLE_DATE_FORMAT);

    private MinioClient minioClient;

    private final Logger logger = LoggerFactory.getLogger(Global.class);

    @Autowired
    @Lazy
    DemoDataSeeder demoDataSeeder;

    @Value("${todospring.minio.url:}")
    private String minioURL;

    @Value("${todospring.minio.access-key:}")
    private String minioAccessKey;

    @Value("${todospring.minio.secret-key:}")
    private String minioSecretKey;

    @Value("${todospring.minio.bucket:}")
    private String minioBucket;

    @Value("${todospring.base-url:#{null}}")
    private String BASE_URL;

    @Value("${todospring.base-port:8080}")
    private String BASE_PORT;

    private BCryptPasswordEncoder encoder;

    private SCryptPasswordEncoder encoderScrypt;

    @PostConstruct
    void init() {

        logger.info("Starting Minio connection to URL: %s".formatted(minioURL));
        try {
            minioClient = MinioClient.builder()
                                     .endpoint(minioURL)
                                     .credentials(minioAccessKey, minioSecretKey)
                                     .build();
        } catch (Exception e) {
            logger.warn("Cannot initialize minio service with url:" + minioURL + ", access-key:" + minioAccessKey + ", secret-key:" + minioSecretKey);
        }

        if (minioBucket.equals("")) {
            logger.warn("Cannot initialize minio bucket: " + minioBucket);
            minioClient = null;
        }

        if (BASE_URL == null) BASE_URL = "http://localhost";
        BASE_URL += ":" + BASE_PORT;

        encoder = new BCryptPasswordEncoder();

        encoderScrypt = SCryptPasswordEncoder.defaultsForSpringSecurity_v5_8();
    }

    @Override
    public void run(String... args) throws Exception {
        demoDataSeeder.seedDemoData();
    }

    public MinioClient getMinioClient() {
        return minioClient;
    }

    public String getMinioBucket() {
        return minioBucket;
    }

    public String getBaseURL() {
        return BASE_URL;
    }

    public PasswordEncoder getPasswordEncoder() { return encoderScrypt; }
}
