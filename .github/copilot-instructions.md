# CLAUDE.md - TrackDev Spring Boot API Reference

This document describes the actual architecture and coding patterns used in the TrackDev Spring Boot backend. Use it as a reference when implementing new features or modifying existing code.

---

# Project: TrackDev API

## Purpose & Context
Educational project tracking platform REST API that manages subjects, courses, projects, tasks, sprints, and team collaboration. Supports three user roles (Admin, Professor, Student) with JWT-based authentication. Serves web and mobile clients through a unified REST API.

## Tech Stack & Build
- **Framework**: Spring Boot 3.5.9
- **Language**: Java 21 LTS
- **Build Tool**: Gradle (with `build.gradle`)
- **Database**: MySQL (hibernate auto DDL)
- **Authentication**: JWT (io.jsonwebtoken:jjwt) with sliding session refresh
- **DTO Mapping**: MapStruct (not manual mapping methods)
- **API Documentation**: Springdoc OpenAPI 2.x (Swagger 3.0)
- **Testing**: JUnit 5, Mockito
- **Localization**: Spring MessageSource (messages.properties, messages_ca.properties, messages_es.properties)

## Project Structure
```
src/
├── main/java/org/trackdev/api/
│   ├── Application.java             # Main entry point
│   ├── controller/                   # REST controllers + exceptions
│   │   ├── exceptions/               # Custom exception classes
│   │   │   ├── BaseException.java    # Abstract base exception
│   │   │   ├── EntityNotFound.java   # 404 exceptions
│   │   │   ├── ServiceException.java # 400 business rule violations
│   │   │   ├── ControllerException.java
│   │   │   └── RestResponseEntityExceptionHandler.java  # Global exception handler
│   │   ├── BaseController.java       # Common controller utilities
│   │   ├── CrudController.java       # Generic CRUD with search capability
│   │   ├── TaskController.java       # Task management endpoints
│   │   └── ...Controller.java
│   ├── service/                      # Business logic layer
│   │   ├── IBaseService.java         # Base service interface
│   │   ├── BaseServiceLong.java      # Common service for Long ID entities
│   │   ├── BaseServiceUUID.java      # Common service for UUID entities
│   │   ├── AccessChecker.java        # Centralized authorization checks
│   │   └── ...Service.java
│   ├── repository/                   # Spring Data JPA repositories
│   │   ├── BaseRepositoryLong.java   # Base for Long ID repositories
│   │   ├── BaseRepositoryUUID.java   # Base for UUID repositories
│   │   └── ...Repository.java
│   ├── entity/                       # JPA entities
│   │   ├── BaseEntityLong.java       # Base entity with Long ID
│   │   ├── BaseEntityUUID.java       # Base entity with UUID
│   │   └── ...Entity classes
│   ├── dto/                          # Data Transfer Objects (response only)
│   ├── mapper/                       # MapStruct mappers
│   ├── model/                        # Request objects, error models
│   ├── configuration/                # Spring Security, JWT, CORS, etc.
│   ├── query/                        # RSQL query parsing (CriteriaParser, etc.)
│   ├── serializer/                   # Custom Jackson serializers
│   └── utils/                        # Error constants, utilities
├── main/resources/
│   ├── application.properties        # Configuration
│   ├── messages.properties           # i18n (English)
│   ├── messages_ca.properties        # i18n (Catalan)
│   ├── messages_es.properties        # i18n (Spanish)
│   └── keystore/                     # JWT key storage
└── test/java/org/trackdev/api/       # Test classes
```

---

## Architecture Pattern: 4-Tier Request Flow

### 1. Controller Layer (`controller/` package)
**Responsibility**: HTTP boundary, request/response mapping, input validation

**Patterns Used**:
- Controllers extend `CrudController<Entity, Service>` for common CRUD + search functionality
- `@Autowired` field injection (not constructor injection)
- MapStruct mappers injected for Entity→DTO transformation
- Returns DTOs directly (not wrapped in ResponseEntity except for specific cases)
- `AccessChecker` service injected for authorization checks
- Swagger annotations: `@Tag`, `@Operation`, `@SecurityRequirement`

**Actual Example** (from TaskController.java):
```java
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "6. Tasks")
@RestController
@RequestMapping(path = "/tasks")
public class TaskController extends CrudController<Task, TaskService> {

    @Autowired
    AccessChecker accessChecker;

    @Autowired
    TaskMapper taskMapper;

    @Operation(summary = "Get information of tasks")
    @GetMapping
    public TasksResponseDTO search(Principal principal,
                         @RequestParam(required = false) String search) {
        String userId = super.getUserId(principal);
        accessChecker.checkCanViewAllTasks(userId);
        return new TasksResponseDTO(taskMapper.toBasicDTOList(super.search(search)));
    }

    @Operation(summary = "Get information of a specific task")
    @GetMapping(path = "/{id}")
    public TaskWithPointsReview getTask(Principal principal, @PathVariable Long id) {
        String userId = super.getUserId(principal);
        Task task = service.getTask(id, userId);
        List<PointsReview> pointsReview = pointsReviewService.getPointsReview(userId);
        return new TaskWithPointsReview(taskMapper.toWithProjectDTO(task), pointsReview);
    }

    @Operation(summary = "Edit task information")
    @PatchMapping(path = "/{id}")
    public TaskCompleteDTO editTask(Principal principal,
                           @PathVariable Long id,
                           @Valid @RequestBody MergePatchTask taskRequest) {
        // Validation at controller level for simple constraints
        if (taskRequest.name != null) {
            if (taskRequest.name.get().isEmpty() || taskRequest.name.get().length() > Task.NAME_LENGTH) {
                throw new ControllerException(ErrorConstants.INVALID_TASK_NAME_LENGTH);
            }
        }
        String userId = super.getUserId(principal);
        return taskMapper.toCompleteDTO(service.editTask(id, taskRequest, userId));
    }
}
```

### 2. Service Layer (`service/` package)
**Responsibility**: Business logic, data coordination, transaction management

**Patterns Used**:
- Services extend `BaseServiceLong<Entity, Repository>` or `BaseServiceUUID<Entity, Repository>`
- `@Autowired` field injection for dependencies
- `@Transactional` on write methods (not class-level)
- Authorization via `AccessChecker` service
- Throws `ServiceException` or `EntityNotFound` with `ErrorConstants` messages
- Returns entities (controller uses mappers to convert to DTOs)

**Actual Example** (from TaskService.java):
```java
@Service
public class TaskService extends BaseServiceLong<Task, TaskRepository> {

    @Autowired
    ProjectService projectService;

    @Autowired
    UserService userService;

    @Autowired
    AccessChecker accessChecker;

    @Transactional
    public Task createTask(Long projectId, String name, String userId) {
        Project project = projectService.get(projectId);
        User user = userService.get(userId);
        accessChecker.checkCanViewProject(project, userId);
        
        Task task = new Task(name, user);
        task.setType(TaskType.USER_STORY);
        task.setProject(project);
        project.addTask(task);
        this.repo.save(task);
        return task;
    }

    @Transactional
    public Task selfAssignTask(Long taskId, String userId) {
        Task task = get(taskId);  // Uses inherited get() from BaseServiceLong
        User user = userService.get(userId);
        accessChecker.checkCanSelfAssignTask(task, userId);
        
        String oldValue = task.getAssignee() != null ? task.getAssignee().getUsername() : null;
        task.setAssignee(user);
        repo.save(task);
        
        // Record the change for audit
        TaskChange change = new TaskAssigneeChange(user.getEmail(), task.getId(), oldValue, user.getUsername());
        taskChangeService.store(change);
        return task;
    }
}
```

### 3. Repository Layer (`repository/` package)
**Responsibility**: Data access abstraction, database queries

**Patterns Used**:
- Extend `BaseRepositoryLong<Entity>` (provides JpaRepository + JpaSpecificationExecutor)
- Use **derived query methods** (Spring Data naming convention) - NO @Query unless necessary
- Keep repositories minimal - most inherit everything from base

**Actual Example** (from TaskRepository.java - simple):
```java
@Repository
public interface TaskRepository extends BaseRepositoryLong<Task> {
}
```

**Actual Example** (from CourseInviteRepository.java - with derived queries):
```java
public interface CourseInviteRepository extends BaseRepositoryLong<CourseInvite> {

    Optional<CourseInvite> findByToken(String token);

    Collection<CourseInvite> findByCourseId(Long courseId);

    // Derived query with enum parameter
    Collection<CourseInvite> findByEmailAndStatus(String email, InviteStatus status);

    Optional<CourseInvite> findByCourseIdAndEmailAndStatus(Long courseId, String email, InviteStatus status);

    Collection<CourseInvite> findByCourseIdAndStatus(Long courseId, InviteStatus status);
}
```

**When to use @Query**: Only for complex queries that can't be expressed as derived queries:
- OR conditions: `findByOwnerIdOrSubjectOwnerId`
- Native SQL with database functions: `sysdate()`
- Complex joins

### 4. Entity Layer (`entity/` package)
**Responsibility**: JPA entity definitions, database mappings

**Patterns Used**:
- Extend `BaseEntityLong` (provides Long id) or `BaseEntityUUID`
- NO Lombok @Data/@Builder on entities - use manual getters/setters/constructors
- Constants for validation: `public static final int NAME_LENGTH = 100;`
- Relationships use `FetchType.LAZY` (collection default)

**Actual Example** (from Task.java):
```java
@Entity
@Table(name = "tasks")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Task extends BaseEntityLong {

    public static final int MIN_NAME_LENGTH = 1;
    public static final int NAME_LENGTH = 100;

    @NonNull
    @Column(length = NAME_LENGTH)
    private String name;

    @ManyToOne
    @JoinColumn(name = "projectId")
    private Project project;

    @ManyToOne
    private User reporter;

    @Column(columnDefinition = "TEXT")
    private String description;

    private TaskType type;
    private Date createdAt;

    @ManyToOne
    private User assignee;

    private Integer estimationPoints;

    @Column(name = "`status`")
    private TaskStatus status;

    @OneToMany(mappedBy = "parentTask")
    private Collection<Task> childTasks;

    @ManyToOne
    @JoinColumn(name = "parentTaskId")
    private Task parentTask;

    // Constructors
    public Task() {}

    public Task(String name, User reporter) {
        this.name = name;
        this.createdAt = new Date();
        this.reporter = reporter;
        this.status = TaskStatus.BACKLOG;
        this.estimationPoints = 0;
        this.rank = 0;
    }

    // Getters and setters (manual, not Lombok)
    @NonNull
    public String getName() { return name; }
    public void setName(@NonNull String name) { this.name = name; }
    // ... more getters/setters
}
```

---

## Data Transfer Objects (DTOs) - `dto/` Package

**Naming Convention**: 
- Response DTOs: `{Entity}BasicDTO`, `{Entity}CompleteDTO`, `{Entity}WithProjectDTO`, `{Entity}SummaryDTO`
- Request objects are in `model/` package: `MergePatch{Entity}`, `Create{Entity}Request`

**Patterns Used**:
- DTOs use `@Data` from Lombok (only DTOs, not entities!)
- NO static factory methods - use MapStruct mappers instead
- Nested DTOs for related entities (e.g., `UserSummaryDTO` inside `TaskBasicDTO`)

**Actual Example** (from TaskBasicDTO.java):
```java
@Data
public class TaskBasicDTO {
    private Long id;
    private String name;
    private String description;
    private Date createdAt;
    private UserSummaryDTO reporter;
    private UserSummaryDTO assignee;
    private String status;
    private String statusText;
    private Integer estimationPoints;
    private Integer rank;
    private Collection<TaskBasicDTO> childTasks;
    private TaskBasicDTO parentTask;
    private Collection<SprintBasicDTO> activeSprints;
}
```

---

## MapStruct Mappers - `mapper/` Package

**Pattern**: Use MapStruct interfaces for Entity→DTO transformation

**Actual Example** (from TaskMapper.java):
```java
@Mapper(componentModel = "spring", uses = {UserMapper.class, SprintMapper.class, CommentMapper.class, ProjectMapper.class})
public interface TaskMapper {

    @Named("taskToBasicDTO")
    @Mapping(target = "status", source = "status", qualifiedByName = "taskStatusToString")
    @Mapping(target = "statusText", source = "statusText")
    @Mapping(target = "reporter", source = "reporter", qualifiedByName = "userToSummaryDTO")
    @Mapping(target = "assignee", source = "assignee", qualifiedByName = "userToSummaryDTO")
    @Mapping(target = "activeSprints", source = "activeSprints", qualifiedByName = "sprintToBasicDTO")
    @Mapping(target = "parentTask", ignore = true)
    @Mapping(target = "childTasks", ignore = true)
    TaskBasicDTO toBasicDTO(Task task);

    @Named("taskToCompleteDTO")
    @Mapping(target = "status", source = "status", qualifiedByName = "taskStatusToString")
    // ... more mappings
    TaskCompleteDTO toCompleteDTO(Task task);

    @IterableMapping(qualifiedByName = "taskToBasicDTO")
    List<TaskBasicDTO> toBasicDTOList(List<Task> tasks);

    @Named("taskStatusToString")
    default String statusToString(TaskStatus status) {
        return status != null ? status.name() : null;
    }
}
```

**Key Points**:
- `componentModel = "spring"` makes mapper injectable with `@Autowired`
- `uses = {...}` composes other mappers
- `@Named` allows referencing specific mapping methods
- `@IterableMapping` for collection conversions

---

## Exception Handling

**Standard Error Response Structure** (from ErrorEntity.java):
```json
{
  "timestamp": "2025-01-05T09:19:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Email is required",
  "code": "SERVICE_ERROR",
  "path": "/tasks/123"
}
```

### Exception Hierarchy
```
BaseException (abstract)
├── ServiceException      → 400 Bad Request (business rule violations)
├── EntityNotFound        → 404 Not Found
├── ControllerException   → 400 Bad Request (input validation)
└── AuthorizedException   → 403 Forbidden
```

**Actual Example** (from BaseException.java):
```java
public abstract class BaseException extends RuntimeException {
    private final HttpStatus httpStatus;
    private final String errorCode;

    protected BaseException(String message, HttpStatus httpStatus, String errorCode) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }

    public HttpStatus getHttpStatus() { return httpStatus; }
    public String getErrorCode() { return errorCode; }
}
```

**Actual Example** (from EntityNotFound.java):
```java
public class EntityNotFound extends BaseException {
    public EntityNotFound() {
        super("No such entity", HttpStatus.NOT_FOUND, "ENTITY_NOT_FOUND");
    }

    public EntityNotFound(String message) {
        super(message, HttpStatus.NOT_FOUND, "ENTITY_NOT_FOUND");
    }

    public EntityNotFound(String entityType, Object id) {
        super(entityType + " with id '" + id + "' not found", HttpStatus.NOT_FOUND, "ENTITY_NOT_FOUND");
    }
}
```

### Error Constants (from ErrorConstants.java)
All error messages are centralized:
```java
public final class ErrorConstants {
    public static final String USER_NOT_FOUND = "User does not exist <%s>";
    public static final String UNAUTHORIZED = "User is not authorized to perform this action or view this resource";
    public static final String ENTITY_NOT_EXIST = "Entity does not exist";
    public static final String INVALID_TASK_NAME_LENGTH = "Task name must be between 1 and 100 characters";
    public static final String STUDENT_ALREADY_ENROLLED = "Student is already enrolled in this course";
    // ... many more
}
```

### Global Exception Handler (RestResponseEntityExceptionHandler.java)
- `@RestControllerAdvice` with `@Order(Ordered.HIGHEST_PRECEDENCE)`
- Extends `ResponseEntityExceptionHandler` to handle Spring MVC exceptions
- All exceptions return `ErrorEntity` response
- Overrides default Spring handlers to maintain consistent format

---

## JWT Authentication & Authorization

### Security Configuration
- **Stateless sessions**: No server-side session storage
- **JWT in Authorization header**: `Bearer <token>`
- **Sliding session**: JWTTokenRefreshFilter issues new token when approaching expiry

### Authorization Pattern: AccessChecker Service
Centralized authorization checks in `AccessChecker.java`:
```java
@Service
public class AccessChecker {
    @Autowired UserService userService;

    // Admin-only checks
    public void checkCanViewAllTasks(String userId) {
        User user = userService.get(userId);
        if (!user.isUserType(UserType.ADMIN)) {
            throw new ServiceException(ErrorConstants.UNAUTHORIZED);
        }
    }

    // Role-based checks
    public void checkCanViewProject(Project project, String userId) {
        // Logic to verify user has access to project
    }

    public void checkCanSelfAssignTask(Task task, String userId) {
        // Verify user is project member and task is unassigned
    }
}
```

**Usage in Controllers/Services**:
```java
accessChecker.checkCanViewProject(project, userId);  // Throws if unauthorized
```

---

## API Testing with Postman Collections

**Location**: Project root

**Files**:
- `TrackDev-API-Tests.postman_collection.json` - All endpoints
- `TrackDev-API-Test.postman_environment.json` - Test environment

**Testing Workflow**:
1. Run API: `./gradlew bootRun` or use `run-server.ps1`
2. Open Postman, import collection and environment
3. Run requests manually or use Collection Runner
4. Use `run-api-tests.ps1` for automated testing

---

## Gradle Build Configuration

**Key gradle tasks**:
```bash
# Run tests
./gradlew test

# Run application
./gradlew bootRun

# Build JAR
./gradlew build

# Compile only (quick check)
./gradlew compileJava --quiet
```

**build.gradle dependencies** (actual):
- Spring Boot Web, Data JPA, Security, Validation
- JWT (io.jsonwebtoken:jjwt-api, jjwt-impl, jjwt-jackson)
- MySQL driver
- Lombok
- MapStruct
- Springdoc OpenAPI

---

## HTTP Status Codes Used

- **200 OK** - Successful GET, PUT, PATCH
- **201 Created** - Successful POST
- **204 No Content** - Successful DELETE
- **400 Bad Request** - Validation error, business rule violation (ServiceException)
- **401 Unauthorized** - Missing/invalid JWT token
- **403 Forbidden** - Valid JWT but lacks permission
- **404 Not Found** - Resource not found (EntityNotFound)
- **500 Internal Server Error** - Unexpected exception

---

## Common Tasks

### Adding a New REST Endpoint

1. **Create DTOs** in `dto/`
   - `{Entity}BasicDTO.java`, `{Entity}CompleteDTO.java` with `@Data` (Lombok)

2. **Create Mapper** in `mapper/`
   - `{Entity}Mapper.java` using MapStruct interface

3. **Implement Service Method** in `service/`
   - Extend `BaseServiceLong<Entity, Repository>` or create standalone service
   - Add method with `@Transactional` if modifying data
   - Use `AccessChecker` for authorization
   - Throw `ServiceException` or `EntityNotFound` on errors
   - Return entity (controller will map to DTO)

4. **Add Controller Method** in `controller/`
   - Extend `CrudController<Entity, Service>` for CRUD with search
   - Inject mapper with `@Autowired`
   - Add `@Operation`, `@Tag`, `@SecurityRequirement` for OpenAPI
   - Return DTO (not ResponseEntity in most cases)

5. **Run Compile Check**
   - `./gradlew compileJava --quiet`

### Running Tests

Linux
```bash
./run-api-tests.sh
```

Windows PowerShell
```powerhell
.\run-api-tests.ps1
```

### Running the server

Linux
```bash
./run-server.sh
```

Windows PowerShell
```powerhell
.\run-server.ps1
```


---

## Important Files Reference

- `src/main/resources/application.properties` - Database, JWT config, server port
- `build.gradle` - Dependencies
- `src/main/resources/messages.properties` - i18n messages (EN)
- `src/main/resources/messages_ca.properties` - i18n messages (Catalan)
- `src/main/resources/messages_es.properties` - i18n messages (Spanish)

---


## Entity constraints

- Tasks
  - Name: 1-100 characters
  - Description: optional
  - Type: Enum (USER_STORY, BUG, TASK)
  - Status: Enum (BACKLOG, TODO, IN_PROGRESS, VERYFINIG, DONE)
  - When a task is created in the Backlog view, it can be of type USER_STORY or TASK or BUG
  - When a task is created as a child of a USER_STORY, it must be of type TASK or BUG
  - A task can only be modified if is is in an active sprint by the assignee of the professor of the course
  - Estimation Points: non-negative integer
  - Manually set for TASK and BUG
  - Automatically calculated as sum of child TASK and BUG points for USER_STORY
  - A USER_STORY cannot be considered as DONE unless all its child TASK and BUG tasks are DONE and all have estimation points
  - A task cannot be moved to DONE status unless it has at least a Pull Request associated (simulated in demo data)

- SprintPattern
    - Name: 1-50 characters
    - Description: optional
    - Duration: 1-12 weeks
    - Start datetime must be before end datetime
    - A SprintPattern contains multiple SprintPatternItems
    - SprintPatternItems in the same SprintPattern cannot overlap in time
    - A SprintPattern is related to a single Course, and Course can contain multiple SprintPattern 

- Sprint
    - Created when a SprintPattern is applied to a project
    - For each SprintPatternItem in the SprintPattern, a Sprint is created
    - Name and description are copied from the pattern item
    - Start and end datetime are copied from the pattern
    - Tasks can only be assigned to sprints that belong to the same project

- Subjects
    - Name: 1-100 characters
    - Description: optional
    - Created by a ADMIN or PROFESSOR user
    - A Subject can have multiple Courses

- Courses
    - Name: 1-100 characters
    - Description: optional
    - Year: 4-digit year
    - Related to a single Subject
    - Created by a PROFESSOR user who becomes the owner of the Course
    - A Course can have multiple enrolled STUDENT users
    - A Course can have multiple Projects

- Projects
    - Name: 1-100 characters
    - Description: optional
    - Related to a single Course
    - Created by the PROFESSOR owner of the Course
    - Before becoming active, a SprintPatter has to be applied to the Project
    - When a SprintPattern is applied, multiple Sprints are created for the Project, one for any SprintPatternItem in the SprintPattern
    - A Project can have multiple Tasks


---

## User Roles and Permissions

- **ADMIN**: Full access to all resources
- **PROFESSOR**: Manages subjects, courses, can view all projects in their courses
- **STUDENT**: specific tasks they can perform:
  - View projects in which they are enrolled
  - Cannot view other projects
  - Can only access tasks in their projects
  - Self-assign unassigned tasks
  - Update task status for tasks assigned to them
  - Update task estimation points for tasks they reported
  - Comment on tasks they have access to


Authorization is centralized in `AccessChecker.java`.

---

## Key Domain Concepts

- **Subject**: Academic subject (e.g., "Software Engineering")
- **Course**: Instance of subject for a specific year
- **Project**: Team project within a course
- **Sprint**: Time-boxed iteration with tasks
- **SprintPattern**: Template for creating sprints
- **Task**: Work item (USER_STORY, BUG, etc.)
- **CourseInvite**: Email invitation to join a course

---

**This file documents the actual patterns used in TrackDev. All examples are from the real codebase.**
