### **Test Cases: Lesson Management**

#### **Preconditions for Lesson Tests:**
*   Ensure there are at least 3 active students in the system (e.g., created via TC1.1/1.2 from Student Management). For example:
    *   Student A (ID 1): Individual Price 25.00 USD, Group Price 20.00 USD
    *   Student B (ID 2): Individual Price 30.00 PLN, Group Price 24.00 PLN
    *   Student C (ID 3): Individual Price 0.00 EUR, Group Price 0.00 EUR (for testing FREE lessons)

#### **1. Create New Lesson**

*   **TC1.1: Create a new lesson with valid data**
    *   **Steps:**
        1.  Navigate to the home page (`/`).
        2.  Click the "Lessons" button.
        3.  On the lesson list page, click "Add New Lesson".
        4.  Fill in valid Date, Time, and Topic (e.g., Date: tomorrow, Time: 10:00, Topic: "English Grammar") and at least one student.
        5.  Leave "Select Students" empty.
        6.  Click "Save Lesson".
    *   **Expected Result:**
        *   Redirects to the lesson list page (`/lessons/list`).
        *   The new lesson is visible in the list with students.

*   **TC1.2: Create a new lesson with one student (individual pricing)**
    *   **Steps:**
        1.  Navigate to "Add New Lesson" form.
        2.  Fill in valid Date, Time, and Topic.
        3.  Select one active student (e.g., Student A).
        4.  Click "Save Lesson".
    *   **Expected Result:**
        *   Redirects to the lesson list page (`/lessons/list`).
        *   The new lesson is visible in the list with "Students Count: 1" and "Payment Status: UNPAID".
        *   **Verification (Student Profile):** Navigate to Student A's profile. Their balance for USD should show -25.00 USD (original balance - individual price).

*   **TC1.3: Create a new lesson with multiple students (group pricing)**
    *   **Steps:**
        1.  Navigate to "Add New Lesson" form.
        2.  Fill in valid Date, Time, and Topic.
        3.  Select two active students (e.g., Student A and Student B).
        4.  Click "Save Lesson".
    *   **Expected Result:**
        *   Redirects to the lesson list page (`/lessons/list`).
        *   The new lesson is visible in the list with "Students Count: 2" and "Payment Status: UNPAID".
        *   **Verification (Student Profiles):**
            *   Navigate to Student A's profile. Their balance for USD should show -20.00 USD (original balance - group price).
            *   Navigate to Student B's profile. Their balance for PLN should show -24.00 PLN (original balance - group price).

*   **TC1.4: Create a new lesson with a student whose individual/group price is 0 (FREE status)**
    *   **Steps:**
        1.  Navigate to "Add New Lesson" form.
        2.  Fill in valid Date, Time, and Topic.
        3.  Select Student C (Individual Price 0.00 EUR).
        4.  Click "Save Lesson".
    *   **Expected Result:**
        *   Redirects to the lesson list page (`/lessons/list`).
        *   The new lesson is visible in the list with "Students Count: 1" and "Payment Status: FREE".
        *   **Verification (Student Profile):** Navigate to Student C's profile. Their balance for EUR should remain 0.00 EUR (no debit for free lesson).

#### **2. View Lesson List**

*   **TC2.1: View lesson list and verify payment statuses**
    *   **Steps:**
        1.  Navigate to the `/lessons/list` page.
    *   **Expected Result:**
        *   All created lessons are visible.
        *   The "Students" column displays the correct names of students for each lesson.
        *   The "Payment Status" column displays the correct status (`PAID`, `UNPAID`, `FREE`, `PARTIALLY_PAID`) with appropriate color coding.

#### **3. View Lesson Profile**

*   **TC3.1: View profile of a lesson from the list**
    *   **Steps:**
        1.  Navigate to the `/lessons/list` page.
        2.  Click on any lesson's row in the table.
    *   **Expected Result:**
        *   Redirects to that lesson's profile page (`/lessons/profile/{id}`).
        *   The page displays correct lesson information (Date, Time, Topic).
        *   The "Students Enrolled" section lists all students associated with the lesson, showing their names and individual `paymentStatus` (e.g., "John Doe - UNPAID").

#### **4. Edit Lesson**

*   **TC4.1: Update lesson details (topic, date, time)**
    *   **Steps:**
        1.  Open any lesson's profile (e.g., from TC3.1).
        2.  Click "Edit Lesson".
        3.  Change the "Topic", "Lesson Date", or "Start Time".
        4.  Click "Update Lesson".
    *   **Expected Result:**
        *   Redirects back to the lesson profile page.
        *   The lesson profile displays the updated details.
        *   Students enrolled and their payment statuses remain unchanged.

*   **TC4.2: Add students to an existing lesson**
    *   **Steps:**
        1.  Create a lesson with one student (e.g., Lesson X with Student A, status UNPAID, balance -25.00 USD).
        2.  Open Lesson X's profile and click "Edit Lesson".
        3.  Select an additional student (e.g., Student B).
        4.  Click "Update Lesson".
    *   **Expected Result:**
        *   Redirects to Lesson X's profile.
        *   "Students Enrolled" now lists Student A and Student B.
        *   **Verification (Student Profiles):**
            *   Student A's balance for USD should change to reflect group price (-20.00 USD - if not already debited for group price).
            *   Student B's balance for PLN should show -24.00 PLN (original balance - group price).
            *   Lesson X's status on the list page should update (e.g., to PARTIALLY_PAID if one student is free, or UNPAID if both are unpaid).

*   **TC4.3: Remove students from an existing lesson**
    *   **Steps:**
        1.  Create a lesson with multiple students (e.g., Lesson Y with Student A and Student B, both UNPAID).
        2.  Open Lesson Y's profile and click "Edit Lesson".
        3.  Deselect Student B.
        4.  Click "Update Lesson".
    *   **Expected Result:**
        *   Redirects to Lesson Y's profile.
        *   "Students Enrolled" now lists only Student A.
        *   **Verification (Student Profiles):**
            *   Student B's balance for PLN should be credited +24.00 PLN (refund of group price). If this payment fully pays off any previous UNPAID lesson for Student B, that lesson's status should change to PAID.
            *   Student A's balance for USD should change to reflect individual price (-25.00 USD - if not already debited for individual price).
            *   Lesson Y's status on the list page should update (e.g., to UNPAID for Student A).

*   **TC4.4: Change students from individual to group pricing (add a second student)**
    *   **Steps:**
        1.  Create Lesson L1 with Student A (Individual Price 25.00 USD).
        2.  Verify Student A's balance is -25.00 USD.
        3.  Open Lesson L1 profile, click "Edit Lesson".
        4.  Add Student B to Lesson L1.
        5.  Click "Update Lesson".
    *   **Expected Result:**
        *   Redirects to Lesson L1 profile.
        *   Both Student A and Student B are listed.
        *   **Verification (Student Profiles):**
            *   Student A's balance for USD should change from -25.00 USD to -20.00 USD (original balance for individual lesson price was credited +25.00 USD and then debited -20.00 USD for group lesson price). The net change on balance for Student A would be +5.00 USD compared to the individual price.
            *   Student B's balance for PLN should be debited -24.00 PLN (group price).
            *   Lesson L1's status on the list page should be UNPAID.

*   **TC4.5: Change students from group to individual pricing (remove all but one student)**
    *   **Steps:**
        1.  Create Lesson L2 with Student A and Student B (group pricing, Balances: Student A -20.00 USD, Student B -24.00 PLN).
        2.  Open Lesson L2 profile, click "Edit Lesson".
        3.  Remove Student B from Lesson L2.
        4.  Click "Update Lesson".
    *   **Expected Result:**
        *   Redirects to Lesson L2 profile.
        *   Only Student A is listed.
        *   **Verification (Student Profiles):**
            *   Student B's balance for PLN should be credited +24.00 PLN (refund of group price).
            *   Student A's balance for USD should change from -20.00 USD to -25.00 USD (original balance for group lesson price was credited +20.00 USD and then debited -25.00 USD for individual lesson price). The net change on balance for Student A would be -5.00 USD compared to the group price.
            *   Lesson L2's status on the list page should be UNPAID.

#### **5. Delete Lesson**

*   **TC5.1: Delete a lesson with students (verify balance changes)**
    *   **Steps:**
        1.  Create a lesson with Student A (e.g., from TC1.1, balance -25.00 USD).
        2.  (Optional: Add a Payment for Student A to make balance sufficient and change lesson status to PAID, e.g., 30.00 USD payment, balance becomes +5.00 USD, lesson status PAID).
        3.  Open the lesson profile and click "Delete Lesson".
        4.  Confirm the action.
    *   **Expected Result:**
        *   Redirects to the lesson list page.
        *   The lesson is no longer visible.
        *   **Verification (Student Profile):** Navigate to Student A's profile. Their balance should be credited +25.00 USD (original balance + lesson price). If any other UNPAID lessons exist, this credit should be used to automatically pay them off.
