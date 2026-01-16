# handoff.md (canonical schema v1.1)

## Context Snapshot
- The project is a stable Spring Boot application for tutor administration with all core features (Student, Lesson, Payment, Balance, Reports) implemented and validated.
- The architecture has been successfully refactored from an event-driven model to a more straightforward direct service-call architecture.
- A comprehensive testing suite is in place, covering unit, integration (`@DataJpaTest`, `@WebMvcTest`), and complete end-to-end business flows (`StudentLessonFlowIT`).
- The entire development lifecycle is automated via a GitHub Actions CI/CD pipeline, which builds, tests, and deploys the application to production.
- The current project focus has shifted from major feature development to ensuring robustness through extensive testing and minor UI/UX enhancements.
- The view `lesson-profile.html` use JavaScript to dynamically update the attended status of a student.

## Active Task(s)
- No active tasks.

## Decisions Made
- **Refactored to Direct Service Calls:** The initial event-driven architecture for balance updates was removed in favor of direct service calls to simplify logic and improve traceability. (link: Design.md §1)
- **Consolidated Balance Logic:** Created a single, generic `BalanceService.changeBalance` method to handle all balance modifications, reducing code duplication. (link: Design.md §3)
- **Standardized Integration Testing:** Adopted a two-pronged testing strategy: integration flow tests (`StudentLessonFlowIT`, `AttendanceFlowIT`) run on a clean, empty database, while individual repository tests (`*RepositoryIT`) use a pre-populated dataset (`data-test.sql`) for consistency. (link: Design.md §2)

## Changes Since Last Session
- Completed T-020: Cover `AtLeastOnePriceNotNullValidator` with unit tests
  - `src/test/java/.../student/validation/AtLeastOnePriceNotNullValidatorTest.java`: Added comprehensive unit tests for the validator.
  - Resolved JaCoCo coverage violation for `AtLeastOnePriceNotNullValidator`.
- Completed T-018: Add payment status of lesson to report
  - `src/main/java/.../report/ReportItemDto.java`: Added `paymentStatus` field.
  - `src/main/java/.../report/ReportService.java`: Populated `paymentStatus` in `ReportItemDto` for lesson items.
  - `src/main/resources/templates/report/view-report.html`: Added a new column for "Payment Status" with conditional display and styling.
- Completed T-019: Add default date values to report filter
  - `src/main/java/.../report/ReportViewController.java`: Added default `startDate` (first day of current month) and `endDate` (current date) to the model for `list-reports.html`.
  - `src/main/resources/templates/report/list-reports.html`: Modified date input fields to use `th:value` for displaying default `startDate` and `endDate`.
- Completed T-015: Implement the ability to calculate the cost of a group lesson if a student misses the lesson
This session focused on implementing new business logic and unit tests.
- `src/main/java/.../lesson/LessonViewController.java`: Add new logic for attendance end-point.
- `src/main/java/.../lesson/LessonService.java`: Add new logic for update attendance of students.
- `src/main/java/.../lesson/PaymentStatusUtil.java`: Change logic for calculating payment status with absent students.
- `src/test/java/**`: Add new tests for attendance logic.
- **Refactored and fixed integration tests in `AttendanceFlowIT.java` to use dynamically created IDs for students and lessons, ensuring robustness and reliability across all test scenarios.**
- Completed T-017: Change field validation in add-student
This session involved updating the database schema and UI.
- `src/main/resources/db/changelog/changeset/005-alter-student-price-nullable.yaml`: Created a new Liquibase changeset to drop NOT NULL constraints for `price_group` and `price_individual` columns in the `students` table.
- `src/main/resources/db/changelog/db.changelog-master.yaml`: Included the new changeset in the master changelog.
- `src/main/resources/templates/student/student-profile.html`: Modified to conditionally display individual and group prices only if they are not null.

## Validation & Evidence
- **Unit & Integration Tests:** All tests passed.
- **Build Status:** `BUILD SUCCESSFUL`
- **Coverage:** JaCoCo reports generated and verified (≥80% lines, ≥75% branches on included classes).
- **Logs/Artifacts:** Build and test results are available in the GitHub Actions workflow logs for the current commit. JaCoCo XML and HTML reports are uploaded as build artifacts. The production application log is located at `logs/admin-events.log` on the target server.

## Risks & Unknowns
- **Database Scalability:** The use of an H2 file-based database in production may pose a risk of data corruption or performance bottlenecks over time, even with a single user. This choice prioritizes simplicity over enterprise-grade robustness. — owner: @asovalov — review: 2026-03-01
- **Performance Testing:** No automated performance benchmarks are in place. Performance regressions could go unnoticed as new features are added. — owner: @asovalov — review: 2026-06-01

## Next Steps
1.  Prioritize and plan the next development cycle, focusing on either addressing the identified risks or developing new features.

## Status Summary
- ✅ — 100%
