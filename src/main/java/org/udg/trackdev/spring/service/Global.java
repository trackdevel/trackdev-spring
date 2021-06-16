package org.udg.trackdev.spring.service;

import io.minio.MinioClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.udg.trackdev.spring.entity.*;
import org.udg.trackdev.spring.configuration.UserType;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Service
public class Global {
    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    private MinioClient minioClient;

    private final Logger logger = LoggerFactory.getLogger(Global.class);

    @Autowired
    private UserService userService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private CourseYearService courseYearService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private IterationService iterationService;

    @Autowired
    private SprintService sprintService;

    @Autowired
    private BacklogService backlogService;

    @Autowired
    private InviteService inviteService;

    @Autowired
    TaskService taskService;

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

        logger.info(String.format("Starting Minio connection to URL: %s", minioURL));
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

        encoderScrypt = new SCryptPasswordEncoder();

        initData();
    }

    private void initData() {
        logger.info("Starting populating database ...");
        User admin = userService.addUserInternal("neich", "ignacio.martin@udg.edu", getPasswordEncoder().encode("123456"), List.of(UserType.ADMIN, UserType.PROFESSOR));
        User student = userService.addUserInternal("student1", "s1@hotmail.com", getPasswordEncoder().encode("0000"), List.of(UserType.STUDENT));
        Invite inviteStudent = inviteService.createInvite("student2@trackdev.com", List.of(UserType.STUDENT), admin.getId());
        Invite inviteProfessor = inviteService.createInvite("professor2@trackdev.com", List.of(UserType.STUDENT), admin.getId());
        Course course = courseService.createCourse("Test course", admin.getId());
        CourseYear courseYear = courseYearService.createCourseYear(course.getId(), 2021,2022, admin.getId());
        Group group = groupService.createGroup("1A", courseYear.getId());
        groupService.addMember(group.getId(), student.getId());
        Iteration iteration = iterationService.create("First iteration", courseYear.getId());
        Sprint sprint = sprintService.create("Sprint 1", iteration.getId(), group.getId());
        Backlog backlog = backlogService.create(group.getId());
        Task task = taskService.create("Task 1", backlog.getId());
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
