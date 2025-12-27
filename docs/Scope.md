# Project Scope: TutorDesk

This document defines the goals, constraints, and success metrics for the TutorDesk application.

## 1. Goals

### 1.1. Primary Goal
The primary goal of TutorDesk is to provide a specialized, all-in-one management application for individual English tutors. It aims to streamline and automate the administrative tasks associated with tutoring, replacing manual methods like spreadsheets.

### 1.2. Functional Goals
- **Student Management:** Centralize student information, including contact details, pricing, and status (active/inactive).
- **Lesson Scheduling:** Enable tutors to schedule lessons, assign multiple students, and track lesson history.
- **Financial Tracking:** Automate student balance calculations. Balances should be debited when a lesson is completed and credited when a payment is received.
- **Payment Management:** Record student payments with details like date, amount, and payment method. The system must automatically reconcile payments against unpaid lessons chronologically.
- **Reporting:** Provide simple, clear reports on key business metrics, such as monthly income, active students, and lesson volume.
- **Usability:** Deliver a clean, intuitive, and user-friendly web interface that is easy to navigate.

### 1.3. Technical Goals
- **Maintainability:** Build a well-structured, layered, and modular application that is easy to understand, test, and extend.
- **Reliability:** Ensure data integrity and accuracy, especially for all financial calculations and status updates.
- **Automation:** Implement a fully automated CI/CD pipeline for building, testing, and deploying the application to production.

### 1.4 Fetcher Goals
- **Test Coverage:** Add jacoco coverage reports to the project and CI pipeline.
- **Multiuser:** Allow multiple tutors to access the application simultaneously.
- **Split Roles:** Allow students to access their own data, while tutors can access all student data.
- **Schedule Lessons:** Allow tutors to schedule lessons and manipulate statuses of a lesson (Scheduled, Canceled, Completed). Consider change balance status when lesson is completed.
- **Implement Google Calendar API:** Allow tutors to get lessons from their Google Calendar.

## 2. Constraints

- **Technology Stack:** The project is committed to the **Spring Boot** ecosystem. All development must use the established stack: Java, Gradle, Spring Data JPA, Thymeleaf, and Spring Security.
- **Architecture:** The application must be developed as a **monolithic, server-side rendered web application**. It is not a Single Page Application (SPA).
- **Deployment Target:** The application is designed to be deployed as a single JAR file on a Linux server, managed by a `systemctl` service.
- **Database:** The primary database for development and production is **H2**. The application relies on its feature set and file-based nature.
- **Target Audience:** The feature set is scoped exclusively for **individual tutors**. It is not intended for multi-user environments, tutoring centers, or schools.
- **User Interface:** The UI is constrained to **Thymeleaf** templates, styled with Bootstrap and custom CSS. Complex client-side interactivity is outside the current scope.

## 3. Success Metrics

### 3.1. Functional Correctness
- **100% Test Pass Rate:** All unit, integration, and flow tests (`StudentLessonFlowIT`) must pass successfully in the CI pipeline.
- **Accurate Financials:** All balance and payment status calculations must be provably correct and match the scenarios outlined in `test-case.md`.
- **Zero Critical Bugs:** The application must be free of critical bugs in the core user flows: student management, lesson creation, payment recording, and report generation.

### 3.2. Code Quality & Maintainability
- **High Test Coverage:** Core business logic within the service layer must have comprehensive test coverage.
- **Adherence to Design:** The codebase must consistently follow the principles defined in `Design.md`, including the layered architecture and use of DTOs.
- **Readability:** Code should be clean, well-documented where necessary, and easy for a new developer to understand.

### 3.3. Operational Stability
- **Reliable Deployments:** The CI/CD pipeline must reliably deploy new versions to production on every push to the `master` branch without manual intervention.
- **Application Uptime:** The production service (`tutordesk.service`) runs without crashes, memory leaks, or a need for frequent restarts.
- **Clean Logs:** Production logs (`logs/admin-events.log`) should be free of repeating, unhandled exceptions.
