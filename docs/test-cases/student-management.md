### **Test Cases: Student Management**

#### **Preconditions for Student Tests:**
*   Start with a clean state (or ensure no students with the same details exist to avoid conflicts).

#### **1. Create New Student**

*   **TC1.1: Create a new student with all valid data**
    *   **Steps:**
        1.  Navigate to the home page (`/`).
        2.  Click the "Students" button.
        3.  On the student list page, click "Add New Student".
        4.  Fill in all fields with valid data:
            *   First Name: `John`
            *   Last Name: `Doe`
            *   Age: `30`
            *   Knowledge Level: `Intermediate`
            *   Country: `USA`
            *   Phone Number: `+1234567890`
            *   Global Goal: `Become fluent in Spanish`
            *   Currency: `USD`
            *   Individual Price: `25.00`
            *   Group Price: `20.00`
        5.  Click "Save Student".
    *   **Expected Result:**
        *   Redirects to the student list page (`/students/list`).
        *   The new student "John Doe" is visible in the list of active students.
        *   **Verification (Database/Profile):** A new `Student` record is created.

*   **TC1.2: Attempt to create a student with missing required fields**
    *   **Steps:**
        1.  Navigate to the "Add New Student" form.
        2.  Fill in only the "Last Name" field. Leave "First Name", "Age", "Knowledge Level", etc., empty.
        3.  Click "Save Student".
    *   **Expected Result:**
        *   The form is re-displayed with validation error messages.
        *   An error message like "First Name is mandatory" is displayed next to the "First Name" field.
        *   Similar messages are displayed for other mandatory fields.
        *   The student is not created.

*   **TC1.3: Attempt to create a student with invalid data**
    *   **Steps:**
        1.  Navigate to the "Add New Student" form.
        2.  Fill in the fields with invalid data:
            *   Age: `-5` (negative value)
            *   Individual Price: `-10.00` (negative value)
        3.  Click "Save Student".
    *   **Expected Result:**
        *   The form is re-displayed with validation error messages.
        *   Error messages indicating the specific validation failures are shown next to the respective fields (e.g., "Size must be between 2 and 50", "must be greater than or equal to 0").
        *   The student is not created.

#### **2. View Student List**

*   **TC2.1: View active students list**
    *   **Steps:**
        1.  Ensure at least one active student exists (e.g., from TC1.1).
        2.  Navigate to the student list page (`/students/list`).
    *   **Expected Result:**
        *   The page displays a table of active students.
        *   The "Show Inactive Students" checkbox is unchecked.
        *   Inactive students are not visible in the list.

*   **TC2.2: View all students (including inactive)**
    *   **Steps:**
        1.  Ensure at least one inactive student exists (e.g., create a student and then deactivate them via TC5.1).
        2.  Navigate to the student list page.
        3.  Check the "Show Inactive Students" checkbox.
    *   **Expected Result:**
        *   The page reloads and now includes all students, both active and inactive.
        *   The "Status" column should clearly differentiate between "Active" and "Inactive" students.

#### **3. View Student Profile**

*   **TC3.1: View profile of an existing student**
    *   **Steps:**
        1.  Navigate to the student list page (`/students/list`).
        2.  Click on any student's name in the table.
    *   **Expected Result:**
        *   Redirects to that student's profile page (`/students/profile/{id}`).
        *   The page displays all correct student details (Name, Age, Prices, Balance, etc.).
        *   The appropriate action buttons ("Update", "Deactivate"/"Activate") are visible based on the student's status.

#### **4. Edit Student**

*   **TC4.1: Update a student's information with valid data**
    *   **Steps:**
        1.  Open any student's profile (e.g., "John Doe" from TC1.1).
        2.  Click "Update Student".
        3.  Change the "Knowledge Level" to "Advanced" and the "Individual Price" to `27.50`.
        4.  Click "Save Student".
    *   **Expected Result:**
        *   Redirects back to the student's profile page.
        *   The profile now displays "Knowledge Level: Advanced" and "Individual Price: $27.50".

*   **TC4.2: Attempt to update a student with invalid data**
    *   **Steps:**
        1.  Open any student's profile and click "Update Student".
        2.  Clear the "First Name" field.
        3.  Enter `-100` for "Age".
        4.  Click "Save Student".
    *   **Expected Result:**
        *   The edit form is re-displayed with validation error messages next to the invalid fields.
        *   The changes are not saved.

#### **5. Deactivate & Activate Student**

*   **TC5.1: Deactivate an active student**
    *   **Steps:**
        1.  Open the profile of an active student.
        2.  Click the "Deactivate Student" button.
        3.  Confirm the action in the popup dialog.
    *   **Expected Result:**
        *   Redirects to the student list page (`/students/list`).
        *   The student is no longer visible in the default list of active students.
        *   **Verification:** Open the student's profile again (e.g., via URL or by showing inactive students). The status should be "Inactive" and the "Activate Student" button should be visible.

*   **TC5.2: Activate an inactive student**
    *   **Steps:**
        1.  Open the profile of an inactive student (from TC5.1).
        2.  Click the "Activate Student" button.
        3.  Confirm the action.
    *   **Expected Result:**
        *   Redirects back to the student's profile page.
        *   The student's status on their profile is now "Active".
        *   The "Deactivate Student" button is visible again.
        *   The student now appears in the default active student list.

#### **6. Hard Delete Student**

*   **TC6.1: Attempt to hard delete an active student**
    *   **Steps:**
        1.  Open the profile of an active student.
    *   **Expected Result:**
        *   The "Hard Delete Student" button is not visible.

*   **TC6.2: Hard delete an inactive student with associated data**
    *   **Steps:**
        1.  Create a new student ("Jane Smith").
        2.  Create a lesson and add "Jane Smith" to it.
        3.  Add a payment for "Jane Smith".
        4.  Deactivate "Jane Smith".
        5.  Navigate to "Jane Smith's" profile.
        6.  Click the "Hard Delete Student" button.
        7.  Confirm the action.
    *   **Expected Result:**
        *   Redirects to the student list page.
        *   The student "Jane Smith" is permanently removed from the system and cannot be found even when showing inactive students.
        *   **Verification (Database):** All records associated with "Jane Smith" are deleted from the `students`, `lesson_student`, `payments`, and `balances` tables. No foreign key constraint errors occur.


When you are ready, we can move on to the next flow, such as "Reports."
