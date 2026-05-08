# TrackDev Spring Boot API

REST API backend for TrackDev educational project management platform. Built with Spring Boot 3, providing secure JWT authentication and comprehensive project, task, and sprint management for academic environments.

## ✨ Features

TrackDev API is a robust backend designed to support educational project management:

### Core Functionality

- **Project Management**: RESTful endpoints for creating and managing team-based software projects
- **Agile Task Tracking**: Complete task lifecycle with user stories, bugs, subtasks, and status transitions
- **Sprint Management**: Sprint pattern templates with automatic sprint generation for projects
- **Team Collaboration**: Task assignment, progress tracking, and team member coordination

### User Roles & Authorization

- **Admin**: System-wide management and oversight capabilities
- **Professor**: Course creation, project setup, student enrollment, and grade management
- **Student**: Project participation, task management, and self-assignment capabilities

### Key Features

- **Course & Subject Management**: Hierarchical organization of courses by academic subjects and years
- **Task Hierarchy**: Parent-child relationships between user stories, tasks, and bugs
- **Sprint Patterns**: Reusable sprint templates with pattern items and automatic application
- **Estimation & Tracking**: Story points calculation, task status tracking, and activity monitoring
- **Course Invitations**: Token-based email invitation system for student enrollment
- **GitHub Integration**: Repository and pull request linking for tasks
- **Secure Authentication**: JWT-based authentication with sliding session refresh
- **Change Tracking**: Audit trail for task modifications and project activity
- **Search & Filtering**: RSQL-based query parsing for advanced search capabilities
- **Push Notifications**: Firebase Cloud Messaging (FCM) delivery to registered mobile devices, with per-user preference flags
- **API Documentation**: Interactive Swagger UI with comprehensive endpoint documentation

## 📦 Project Structure

```
trackdev2-spring/
├── src/
│   ├── main/
│   │   ├── java/org/trackdev/api/
│   │   │   ├── controller/           # REST endpoints
│   │   │   │   └── exceptions/       # Exception handling
│   │   │   ├── service/              # Business logic
│   │   │   ├── repository/           # Data access (JPA)
│   │   │   ├── entity/               # JPA entities
│   │   │   ├── dto/                  # Data Transfer Objects
│   │   │   ├── mapper/               # MapStruct mappers
│   │   │   ├── model/                # Request models
│   │   │   ├── configuration/        # Security, JWT, CORS
│   │   │   ├── query/                # RSQL query parsing
│   │   │   └── utils/                # Utilities and constants
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── messages*.properties  # i18n (en, ca, es)
│   │       └── keystore/             # JWT keys
│   └── test/                         # Unit and integration tests
├── scripts/
│   ├── run-server.ps1                # Windows startup script
│   └── run-server.sh                 # Linux/macOS startup script
├── docs/
│   └── endpoints.md                  # API endpoint documentation
├── build.gradle                      # Gradle build configuration
└── .github/
    └── copilot-instructions.md       # Architecture reference
```

## 🔧 Tech Stack

### Framework & Core

- **Spring Boot**: 3.5.9
- **Java**: 21 LTS
- **Build Tool**: Gradle 8.x
- **Database**: MySQL with Hibernate (auto DDL)

### Key Dependencies

- **Authentication**: JWT (io.jsonwebtoken:jjwt 0.12.7)
- **DTO Mapping**: MapStruct 1.6.3
- **API Docs**: Springdoc OpenAPI 2.8.14 (Swagger 3.0)
- **Validation**: Spring Boot Starter Validation
- **Email**: Spring Boot Starter Mail
- **Security**: Spring Security (Web, Config, Crypto)
- **Testing**: JUnit 5, Mockito, Spring Boot Test
- **Utilities**: Lombok 1.18.42, Apache Commons Lang3

### Architecture

- **4-Tier Pattern**: Controller → Service → Repository → Entity
- **Exception Handling**: Centralized global exception handler
- **Authorization**: `AccessChecker` service for role-based permissions
- **Stateless Sessions**: JWT tokens with no server-side storage
- **Localization**: Spring MessageSource (English, Catalan, Spanish)

## 🚀 Getting Started

### Prerequisites

- **Java**: JDK 21 LTS
- **MySQL**: 8.0+ running instance
- **Gradle**: 8.x (wrapper included)
- **Environment**: Windows, Linux, or macOS

### Step 1: Configure Database

Create MySQL database and user:

```sql
CREATE DATABASE trackdev;
CREATE USER 'trackdev'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON trackdev.* TO 'trackdev'@'localhost';
FLUSH PRIVILEGES;
```

### Step 2: Configure Application

Edit `src/main/resources/application.properties`:

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/trackdev
spring.datasource.username=trackdev
spring.datasource.password=your_password

# Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

# JWT
jwt.secret=your-secret-key-here
jwt.expiration=86400000

# Server
server.port=8080

# Discord Integration
trackdev.discord.client-id=your_client_id
trackdev.discord.client-secret=your_client_secret
trackdev.discord.bot-token=your_bot_token
trackdev.discord.guild-id=your_guild_id
trackdev.discord.verified-role-id=your_role_id
trackdev.discord.redirect-uri=http://localhost:8080/api/discord/callback

# Firebase Cloud Messaging (optional — leave unset to disable push notifications)
# Path to the admin SDK service-account JSON file
trackdev.firebase.service-account-path=/absolute/path/to/firebase-service-account.json
```

When using `scripts/run-server.sh` with a `.env` file, the equivalent variable is:

```
FIREBASE_SERVICE_ACCOUNT_JSON=/absolute/path/to/firebase-service-account.json
```

### Step 3: Run the Server

**Windows PowerShell:**

```powershell
.\scripts\run-server.ps1
```

**Linux/macOS:**

```bash
chmod +x scripts/run-server.sh
./scripts/run-server.sh
```

**Or using Gradle directly:**

```bash
# Run with Gradle wrapper
./gradlew bootRun

# Or on Windows
gradlew.bat bootRun
```

API will be available at `http://localhost:8080`

### Step 4: Access API Documentation

Open Swagger UI in your browser:

```
http://localhost:8080/swagger-ui.html
```

## 🔐 Authentication

JWT-based stateless authentication:

### Login Flow

1. **POST** `/auth/login` with email/password
2. Receive JWT token in response
3. Include token in `Authorization: Bearer <token>` header
4. Token automatically refreshed on approaching expiry

### Example Login Request

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password"}'
```

### Response

```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "user": {
    "id": "user-uuid",
    "email": "user@example.com",
    "username": "john.doe",
    "userType": "STUDENT"
  }
}
```

## 📲 Push Notifications (FCM)

The backend can deliver push notifications to mobile devices via Firebase Cloud Messaging. Notifications are intended primarily for the student-facing mobile app.

### Enabling

1. Generate a Firebase admin SDK service-account JSON from the Firebase Console.
2. Save it to a path the server can read.
3. Set the env var (or property) before starting the server:

   ```
   FIREBASE_SERVICE_ACCOUNT_JSON=/absolute/path/to/firebase-service-account.json
   ```

If the variable is unset (or the file is missing), FCM is **cleanly disabled**: every notification call short-circuits, no error is thrown, and the rest of the API works normally. A warning is logged at startup.

### Device token lifecycle

The mobile app registers each device's FCM token via `POST /users/me/push-tokens`. Tokens are stored in the `user_push_tokens` table. The server:

- Treats `POST` as **idempotent** on token: re-registering an existing token updates owner/platform/lastSeenAt.
- Sends notifications via `sendEachForMulticast` to all of the user's tokens.
- **Auto-deletes stale tokens** when FCM responds with `UNREGISTERED` or `INVALID_ARGUMENT`.

### Triggers

| Event                                    | Recipients                                                | Pref flag             |
| ---------------------------------------- | --------------------------------------------------------- | --------------------- |
| Comment on a task I'm assigned to        | task assignee (if not the comment author)                 | `notifyComments`      |
| Points-review conversation created       | initiator ∪ participants ∪ course owner − actor           | `notifyPointsReview`  |
| New message in a points-review conv.     | initiator ∪ participants ∪ course owner − message author  | `notifyPointsReview`  |
| Participant added to points-review conv. | the newly added user only                                 | `notifyPointsReview`  |
| Pull request merged on a project task    | all project members − actor                               | `notifyTeamActivity`  |
| Task transitions to DONE                 | all project members − actor                               | `notifyTeamActivity`  |

The course owner (professor) is implicitly part of every points-review conversation (role-based access) and is included as a recipient even when not in the explicit `participants` set.

The task → DONE notification fires only on the user's explicit transition; the parent USER_STORY's auto-cascade to DONE is intentionally silent to avoid double-pings per click.

### Per-user preferences

Each user has three boolean preference flags on the `users` table, all defaulting to `true`:

- `notify_comments`
- `notify_points_review`
- `notify_team_activity`

These are exposed via `GET` / `PATCH /users/me/notification-preferences`. Each notification call gates on the recipient's flag — flipping `notifyTeamActivity` to `false` silences PR-merge and task-DONE pushes for that user without affecting anyone else.

### Payload shape

Every notification carries:

- A user-visible `title` and `body` (HTML stripped, body truncated at ~140 chars).
- A `data` map with at least `type` and the relevant entity ids (`taskId`, `taskKey`, `projectId`, `conversationId`, `prId`, etc.) so the mobile app can deep-link to the right screen.

### Reliability

Pushes are dispatched **after the originating database transaction commits** (`TransactionSynchronizationManager.afterCommit`), so a rolled-back action never produces a phantom notification.

## 🔗 API Endpoints

### Authentication

| Method | Endpoint        | Description        | Auth Required |
| ------ | --------------- | ------------------ | ------------- |
| POST   | `/auth/login`   | Login with email   | No            |
| POST   | `/auth/logout`  | Logout user        | Yes           |
| GET    | `/auth/self`    | Get current user   | Yes           |
| POST   | `/auth/refresh` | Refresh JWT token  | Yes           |

### Push Notifications

| Method | Endpoint                              | Description                          | Auth Required |
| ------ | ------------------------------------- | ------------------------------------ | ------------- |
| POST   | `/users/me/push-tokens`               | Register a device push token         | Yes           |
| GET    | `/users/me/push-tokens`               | List own registered tokens           | Yes           |
| DELETE | `/users/me/push-tokens/{token}`       | Unregister a token                   | Yes           |
| GET    | `/users/me/notification-preferences`  | Get notification preference flags    | Yes           |
| PATCH  | `/users/me/notification-preferences`  | Update notification preference flags | Yes           |

### Projects

| Method | Endpoint            | Description             | Auth Required |
| ------ | ------------------- | ----------------------- | ------------- |
| GET    | `/projects`         | List user's projects    | Yes           |
| GET    | `/projects/{id}`    | Get project details     | Yes           |
| POST   | `/projects`         | Create project          | Yes (Prof)    |
| PATCH  | `/projects/{id}`    | Update project          | Yes (Prof)    |
| DELETE | `/projects/{id}`    | Delete project          | Yes (Prof)    |

### Tasks

| Method | Endpoint                  | Description           | Auth Required |
| ------ | ------------------------- | --------------------- | ------------- |
| GET    | `/tasks`                  | List tasks (search)   | Yes           |
| GET    | `/tasks/{id}`             | Get task details      | Yes           |
| POST   | `/tasks`                  | Create task           | Yes           |
| PATCH  | `/tasks/{id}`             | Update task           | Yes           |
| DELETE | `/tasks/{id}`             | Delete task           | Yes           |
| POST   | `/tasks/{id}/self-assign` | Self-assign task      | Yes (Student) |

### Sprints

| Method | Endpoint          | Description        | Auth Required |
| ------ | ----------------- | ------------------ | ------------- |
| GET    | `/sprints`        | List sprints       | Yes           |
| GET    | `/sprints/{id}`   | Get sprint details | Yes           |
| POST   | `/sprints`        | Create sprint      | Yes (Prof)    |
| PATCH  | `/sprints/{id}`   | Update sprint      | Yes (Prof)    |

### Courses

| Method | Endpoint         | Description       | Auth Required |
| ------ | ---------------- | ----------------- | ------------- |
| GET    | `/courses`       | List courses      | Yes           |
| GET    | `/courses/{id}`  | Get course        | Yes           |
| POST   | `/courses`       | Create course     | Yes (Prof)    |
| PATCH  | `/courses/{id}`  | Update course     | Yes (Prof)    |

**Full endpoint documentation**: See [docs/endpoints.md](docs/endpoints.md) or Swagger UI

## 🛠️ Development Commands

### Build & Run

```bash
# Compile Java sources
./gradlew compileJava

# Run tests
./gradlew test

# Build JAR file
./gradlew build

# Run application
./gradlew bootRun

# Clean build outputs
./gradlew clean
```

### Quick Compile Check

```bash
./gradlew compileJava --quiet
```

### Running Tests

**Windows PowerShell:**

```powershell
.\scripts\run-api-tests.ps1
```

**Linux/macOS:**

```bash
./scripts/run-api-tests.sh
```

## 🔨 Common Development Tasks

### Adding a New REST Endpoint

1. **Create DTOs** in `dto/` package (use `@Data` from Lombok)
2. **Create MapStruct Mapper** in `mapper/` package
3. **Implement Service Method** in `service/` (extend `BaseServiceLong` or `BaseServiceUUID`)
   - Add `@Transactional` for write operations
   - Use `AccessChecker` for authorization
   - Throw `ServiceException` or `EntityNotFound` on errors
4. **Add Controller Method** in `controller/` (extend `CrudController` for CRUD+search)
   - Inject mapper and service with `@Autowired`
   - Add Swagger annotations (`@Operation`, `@Tag`, `@SecurityRequirement`)
   - Return DTOs (not wrapped in ResponseEntity)
5. **Test with Postman**: Use provided collections

### Database Changes

Hibernate auto-DDL is enabled (`spring.jpa.hibernate.ddl-auto=update`). Entity changes automatically update schema. For production, use migrations (Flyway/Liquibase).

### Adding Dependencies

Edit `build.gradle`:

```gradle
dependencies {
    implementation 'group:artifact:version'
}
```

Then refresh Gradle: `./gradlew build`

## 📱 API Testing

### Postman Collections

Located in project root:

- `TrackDev-API-Tests.postman_collection.json` - All endpoints
- `TrackDev-API-Test.postman_environment.json` - Test environment

**Workflow:**

1. Import collection and environment into Postman
2. Ensure API is running (`./gradlew bootRun`)
3. Run requests manually or use Collection Runner
4. Use automated scripts: `run-api-tests.ps1` or `run-api-tests.sh`

## 🧩 Architecture Patterns

### 4-Tier Request Flow

1. **Controller Layer**: HTTP boundary, input validation, DTO transformation
2. **Service Layer**: Business logic, transactions, authorization checks
3. **Repository Layer**: Data access (Spring Data JPA with derived queries)
4. **Entity Layer**: JPA entities with relationships

### Key Conventions

- **Controllers**: Extend `CrudController<Entity, Service>` for CRUD+search
- **Services**: Extend `BaseServiceLong<Entity, Repo>` or `BaseServiceUUID<Entity, Repo>`
- **Repositories**: Extend `BaseRepositoryLong<Entity>` or `BaseRepositoryUUID<Entity>`
- **Entities**: Extend `BaseEntityLong` or `BaseEntityUUID` (NO Lombok on entities)
- **DTOs**: Use `@Data` from Lombok, MapStruct for transformation
- **Exceptions**: Use `ServiceException` (400), `EntityNotFound` (404), `AuthorizedException` (403)
- **Error Messages**: Centralized in `ErrorConstants` class

### Authorization

Centralized in `AccessChecker` service:

```java
accessChecker.checkCanViewProject(project, userId);
accessChecker.checkCanSelfAssignTask(task, userId);
```

## 📚 Documentation

- **Architecture Reference**: `.github/copilot-instructions.md` - Comprehensive patterns and examples
- **Endpoint Details**: `docs/endpoints.md` - Full API specification
- **Swagger UI**: `http://localhost:8080/swagger-ui.html` - Interactive API docs
- **OpenAPI Spec**: `http://localhost:8080/v3/api-docs` - OpenAPI JSON

## 🤝 Related Projects

- [TrackDev Frontend (Next.js)](https://github.com/trackdevel/trackdev3) - TypeScript monorepo with web app
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Security](https://spring.io/projects/spring-security)
- [Swagger/OpenAPI](https://swagger.io/specification/)

## 🧪 Testing

Tests use JUnit 5 and Mockito:

```bash
# Run all tests
./gradlew test

# Run tests with coverage report
./gradlew test jacocoTestReport

# View test reports
open build/reports/tests/test/index.html
```

## 📄 License

MIT

---

**Developed for educational purposes** - Teaching agile software development in academic environments.