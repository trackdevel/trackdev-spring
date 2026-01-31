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

### Spring Boot Controller guidelines

When using @RequestParam or @PathVariable in Spring Boot controllers, always specify the parameter name explicitly. This improves code readability and helps avoid potential issues during refactoring. For example, use @RequestParam(name = "id") Long id instead of just @RequestParam Long id.

Controllers should try to enforce a single call to Service layer methods per endpoint. Any additional data fetching or processing should be handled within the Service layer to maintain separation of concerns and ensure operation is transactional when needed.


### 2. Service Layer (`service/` package)
**Responsibility**: Business logic, data coordination, transaction management

**Patterns Used**:
- Services extend `BaseServiceLong<Entity, Repository>` or `BaseServiceUUID<Entity, Repository>`
- `@Autowired` field injection for dependencies
- `@Transactional` on write methods (not class-level)
- Authorization via `AccessChecker` service
- Throws `ServiceException` or `EntityNotFound` with `ErrorConstants` messages
- Returns entities (controller uses mappers to convert to DTOs)


### 3. Repository Layer (`repository/` package)
**Responsibility**: Data access abstraction, database queries

**Patterns Used**:
- Extend `BaseRepositoryLong<Entity>` (provides JpaRepository + JpaSpecificationExecutor)
- Use **derived query methods** (Spring Data naming convention) - NO @Query unless necessary
- Keep repositories minimal - most inherit everything from base

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
- Do not expose foreign key IDs as fields, use relationships instead. 


---

## Data Transfer Objects (DTOs) - `dto/` Package

**Naming Convention**: 
- Response DTOs: `{Entity}BasicDTO`, `{Entity}CompleteDTO`, `{Entity}WithProjectDTO`, `{Entity}SummaryDTO`
- Request objects are in `model/` package: `MergePatch{Entity}`, `Create{Entity}Request`

**Patterns Used**:
- DTOs use `@Data` from Lombok (only DTOs, not entities!)
- NO static factory methods - use MapStruct mappers instead
- Nested DTOs for related entities (e.g., `UserSummaryDTO` inside `TaskBasicDTO`)

---

## MapStruct Mappers - `mapper/` Package

**Pattern**: Use MapStruct interfaces for Entity→DTO transformation

**Key Points**:
- `componentModel = "spring"` makes mapper injectable with `@Autowired`
- `uses = {...}` composes other mappers
- `@Named` allows referencing specific mapping methods
- `@IterableMapping` for collection conversions

---

## Exception Handling

ALL exceptions ocurring in controllers/services should return a consistent error response format with proper HTTP status codes.

### Exception Hierarchy
```
BaseException (abstract)
├── ServiceException      → 400 Bad Request (business rule violations)
├── EntityNotFound        → 404 Not Found
├── ControllerException   → 400 Bad Request (input validation)
└── AuthorizedException   → 403 Forbidden
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

# Run application. Use scripts for easier usage
Linux:
# Development with .env
```./scripts/run-server.sh```ç
# Production
```./scripts/run-server.sh .env.prod```

Windows:
# Development with .env
```.\scripts\run-server.ps1 -EnvFile .env```
# Production
```.\scripts\run-server.ps1 -EnvFile .env.prod -SpringProfile prod```

# Build JAR
./gradlew clean bootJar

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
```powershell, from the root of the project
.\scripts\run-server.ps1 -EnvFile .\.env
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
  - Status: Enum (BACKLOG, TODO, IN_PROGRESS, VERIFYING, DONE)
  - When a task is created in the Backlog view, it can be of type USER_STORY or TASK or BUG
  - When a task is created as a child of a USER_STORY, it must be of type TASK or BUG
  - Subtask type must be passed to the backend `createSubTask` method (not hardcoded)
  - When a task is created as a child of a USER_STORY, it can be created by a STUDENT different than the one assigned to the USER_STORY. The new Task (child) will be assigned to the STUDENT logged in that creates it
  - A task can only be modified if it is in an active sprint by the assignee or the professor of the course
  - Estimation Points: non-negative integer
  - Manually set for TASK and BUG only
  - For USER_STORY: estimation points are always calculated as the sum of subtask estimation points (cannot be set manually)
  - A USER_STORY cannot be considered as DONE unless all its child TASK and BUG tasks are DONE and all have estimation points
  - A task cannot be moved to VERIFYING status unless it has at least 1 Pull Request associated (simulated in demo data)
  - A task cannot be moved to DONE status unless it has at least 1 Pull Request merged (simulated in demo data)
  - Subtasks of USER_STORY include both TASK and BUG types when displayed in sprint board

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
    - **Status is computed dynamically** via `Sprint.getEffectiveStatus()`:
      - If manually set to CLOSED → always CLOSED (manual close sticks)
      - Before startDate → DRAFT (future sprint)
      - Between startDate and endDate → ACTIVE
      - After endDate → CLOSED
    - Use `getEffectiveStatus()` (not `getStatus()`) when checking sprint state in business logic
    - The stored `status` field is only for manual overrides (e.g., force-close)
    - No scheduled job needed for status transitions

- Task status in future sprints:
    - Tasks can be moved from backlog to a future sprint (status becomes TODO)
    - Tasks in a future sprint (DRAFT status) cannot change from TODO until the sprint becomes ACTIVE
    - Tasks in a FUTURE sprint can only be moved to the backlog (not to another sprint)
    - Once at least one sprint containing the task is ACTIVE, status changes are allowed

- Relationship between tasks and sprints:
    - A USER_STORY will belong to any sprint where at least one of its subtasks is assigned. So a USER_STORY can belong to more than 1 sprint
    - If a USER_STORY has ALL its subtasks unassigned to any sprint, the USER_STORY will also be unassigned from any sprint and then the asignee will be able to manually assign it. That will mean that ALL its subtask will be automatically assigned to the same sprint
    - A TASK or BUG (with no parent) can only be assigned to 1 Sprint
    - A TASK or BUG cannot be reassigned to another sprint if they are in state DONE. 
    - A TASK or BUG can be reasigned to another Sprint, provided they are not in DONE state AND the Sprint is either active or in the future
    - A TASK or BUG can be reasigned from the Task View making the Sprint field on the right editable

- Moving tasks back to backlog (removing from sprint):
    - A USER_STORY can only be moved back to backlog if ALL its subtasks are in TODO state
    - A TASK or BUG (with no parent) can only be moved back to backlog if it is in TODO state
    - A subtask (TASK or BUG with a parent) cannot be moved to backlog individually - must move the parent USER_STORY instead
    - If a task has begun (status is not TODO), it cannot go back to the backlog - show error "A task that has begun cannot go back to the backlog"

- Subjects
    - Name: 1-100 characters
    - Description: optional
    - Created by a WORKSPACE_ADMIN user
    - A Subject can have multiple Courses

- Courses
    - Name: 1-100 characters
    - Description: optional
    - Year: 4-digit year
    - Related to a single Subject
    - Created by a PROFESSOR user who becomes the owner of the Course
    - A Course can have multiple enrolled STUDENT users
    - A Course can have multiple Projects

- Workspaces
    - Name: 1-100 characters
    - Description: optional
    - Created by an ADMIN user
    - A Workspace can have multiple PROFESSOR users as members
    - A Workspace can have multiple Subjects
    - Users from one Workspace cannot access resources from another Workspace

- Projects
    - Name: 1-100 characters
    - Description: optional
    - Related to a single Course
    - Created by the PROFESSOR owner of the Course
    - Before becoming active, a SprintPatter has to be applied to the Project
    - When a SprintPattern is applied, multiple Sprints are created for the Project, one for any SprintPatternItem in the SprintPattern
    - A Project can have multiple Tasks

-  Users
    - Username: unique, 3-50 characters (read-only after creation)
    - Email: unique, valid email format
    - FullName: optional, display name for the user
    - Password: stored as bcrypt hash
    - UserType: Enum (ADMIN, WORKSPACE_ADMIN, PROFESSOR, STUDENT)
    - Profile editing: Users can update their own `fullName` and `email` via `PUT /users/me`
    - The `EditU` request class must include all editable fields (fullName, email, password)
    - User creation constraints (it works for UI and API):
        - If as ADMIN I create a new ADMIN, no need of additional information
        - If as ADMIN I create a new WORKSPACE_ADMIN, I only need to select the Workspace to which it will be associated
        - As an ADMIN, I don't want to create STUDENT or PROFESSORS
        - If as a WORKSPACE_ADMIN I want to create a new PROFESSOR, it will be assigned to the admin Workspace, no need to select additional information
        - As a WORKSPACE_ADMIN, I only must be able to create new PROFESSOR
        - As a PROFESSOR, I want to be able to create only STUDENT. I will need to provide the Course to which the STUDENT will be enrolled
    - User editing constraints:
        - Only ADMIN users can change the UserType of another user
        - ADMIN can change/delete any info of WORKSPACE_ADMIN, PROFESSOR, STUDENT users
        - WORKSPACE_ADMIN can change/delete any info of PROFESSOR and STUDENT users in their Workspace
        - PROFESSOR can change/delete any info of STUDENT users in their Courses
        - Additional editing contraints:
            - WORKSPACE_ADMIN can add a PROFESSOR to a Course only if the PROFESSOR belongs to the same Workspace
            - WORKSPACE_ADMIN can create Subjects only in their Workspace
            - WORKSPACE_ADMIN can create Courses only in Subjects that belong to their Workspace
            - PROFESSOR can create Projects only in their Courses
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
