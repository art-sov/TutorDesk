# TutorDesk Project Design

This document outlines the architectural design, technology stack, and core principles of the TutorDesk application.

## 1. High-Level Architecture

TutorDesk is a monolithic web application built using the **Spring Boot** framework. It follows a classic **Layered Architecture** pattern, separating concerns into distinct layers:

- **Presentation Layer (Web/MVC):** Handles HTTP requests, interacts with the service layer, and renders views for the user.
- **Service Layer (Business Logic):** Contains the core business logic of the application. It orchestrates calls to the data access layer and implements application-specific use cases.
- **Data Access Layer (Persistence):** Responsible for data persistence and retrieval. It uses repositories to interact with the underlying database.
- **Domain Model:** A rich set of objects representing the core business entities (e.g., `Student`, `Lesson`, `Payment`).

The application is organized by feature, with packages like `student`, `lesson`, `payment`, and `report` each containing their own controllers, services, repositories, and DTOs. This promotes modularity and makes the codebase easier to navigate and maintain.

## 2. Technology Stack

### Backend
- **Framework:** Spring Boot 3.5.8
- **Language:** Java 25
- **Web:** Spring MVC for handling web requests.
- **Templating Engine:** Thymeleaf for server-side view rendering.
- **Data Persistence:** Spring Data JPA with Hibernate as the persistence provider.
- **Database:** H2 in a file (In-memory database for development and testing). It is possible to switch to PostgreSQL for production.
- **Database Migration:** Liquibase for managing and versioning the database schema.
- **Security:** Spring Security for authentication and authorization.
- **Validation:** Spring Validation (Bean Validation) for data integrity.

### Frontend
- **Styling:** Bootstrap and Plain CSS with some utility classes. The project uses a custom stylesheet located at `src/main/resources/static/css/styles.css`.

### Development & Build Tools
- **Build Tool:** Gradle
- **Code Generation:**
  - **Lombok:** Reduces boilerplate code (getters, setters, constructors, etc.).
  - **MapStruct:** Generates type-safe mappers for converting between entities and Data Transfer Objects (DTOs).
- **Live Reload:** Spring Boot DevTools for rapid development cycles.
- **Testing:**
  - **JUnit 5:** The primary testing framework.
  - **Mockito:** For creating mock objects in unit tests.
  - **Spring Test & @WebMvcTest:** For integration and slice tests of the web and data layers.
  - **Spring Security Test:** For testing security configurations.

## 3. Core Architectural Principles

- **Model-View-Controller (MVC):** The presentation layer is structured using the MVC pattern, with `*ViewController` classes acting as controllers, Thymeleaf templates as views, and DTOs/domain objects as models.
- **Dependency Injection:** The application heavily relies on Spring's Dependency Injection (DI) container to manage component lifecycles and dependencies.
- **Separation of Concerns:** Each layer has a distinct responsibility. Controllers are thin and delegate to services, services contain business logic, and repositories handle database interaction.
- **Data Transfer Objects (DTOs):** DTOs are used to transfer data between the presentation layer and the service layer. This decouples the API/UI from the internal domain model and prevents issues like lazy loading exceptions or exposing internal entity state. MapStruct is used to automate the mapping between entities and DTOs.
- **Repository Pattern:** Spring Data JPA repositories are used to abstract away the details of data access, providing a clean, high-level API for CRUD operations and custom queries.
- **Centralized Exception Handling:** A `@ControllerAdvice` (`GlobalExceptionHandler`) is used to handle exceptions globally, providing a consistent error response mechanism.
- **Configuration over Convention:** The application leverages Spring Boot's autoconfiguration capabilities, minimizing the need for explicit configuration. Custom configurations are centralized in the `config` package.
- **Sensitive data:** All sensitive data (e.g., database credentials, admin login and password) are stored in environment variables, which are accessible only to the application process.

## 4. Logging

The application uses **Logback** for logging, configured in `src/main/resources/logback-spring.xml`.

- **Log Levels:** The root logging level is set to `INFO`. Specific noisy loggers, such as those for Hibernate and certain Spring MVC handlers, are set to `ERROR` to reduce log volume.
- **Appenders:** Two main appenders are configured:
  - `CONSOLE`: Outputs logs to the standard console for real-time monitoring during development.
  - `FILE`: A `RollingFileAppender` that writes logs to `logs/admin-events.log`.
- **Rolling Policy:** The file appender uses a `SizeAndTimeBasedRollingPolicy`. Logs are automatically archived and compressed based on date and size. A new log file is created daily (`%d{yyyy-MM-dd}`) or when the file size reaches 10MB. The system retains a maximum of 7 historical log files and enforces a total size cap of 1GB to manage disk space.

## 5. CI/CD

The project uses **GitHub Actions** for its Continuous Integration and Continuous Deployment (CI/CD) pipeline, defined in `.github/workflows/ci-cd.yml`.

- **Branching Strategy & Triggers:** The workflow is triggered on:
  - `push` to the `dev` and `master` branches.
  - `pull_request` targeting the `master` branch.
  This strategy ensures that code is always tested before integration into the main branches.

- **Continuous Integration (CI):** For all triggered events, the CI job performs the following steps:
  1.  **Checkout & Setup:** Checks out the source code and sets up the Java 25 environment.
  2.  **Build & Test:** Executes the Gradle wrapper (`./gradlew build`) to compile the code, run all unit and integration tests, and build the application JAR. This step is crucial for maintaining code quality and catching regressions early.

- **Continuous Deployment (CD):** Deployment is automatically triggered only on a `push` to the `master` branch, ensuring that only stable, tested code is released.
  1.  **Artifact Upload:** The built `tutordesk.jar` file is uploaded as a build artifact for archival and traceability.
  2.  **Deploy to Server:** The JAR is securely copied (via `scp`) to the production server.
  3.  **Restart Service:** An SSH command is executed on the server to restart the application service (`systemctl restart tutordesk.service`), completing the zero-downtime deployment.


## 6. Specific rules for the project:
*   **MockitoBean Usage:** In all tests, use `org.springframework.test.context.bean.override.mockito.MockitoBean` instead of `org.springframework.boot.test.mock.mockito.MockBean`.
*   **List Access:** In all lists, use `getFirst()` instead of `get(0)` for improved clarity and intent.

## 7. Integration Test Scenarios:
*   **Scenario 1: Student and Lesson Management Flow.**
    *   **Test:** `test_createStudentAndAddLesson`
    *   **Status:** Done
*   **Scenario 2: Payment and Balance Reconciliation Flow.**
    *   **Test:** `test_paymentCoversMultipleLessonsChronologically`
    *   **Status:** Done
*   **Scenario 3: Deletion and Balance Rollback Flow.**
    *   **Test:** `test_deletePaymentAndLessonAndVerifyBalance`
    *   **Status:** Done
*   **Scenario 4: Full Student Lifecycle Flow.**
    *   **Test:** `test_fullStudentLifecycle`
    *   **Status:** Done
