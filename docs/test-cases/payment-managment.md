### **Test Cases: Payment Management**

#### **Preconditions for Payment Tests:**
*   Ensure at least one active student exists (e.g., "Student A" created via Student Management TC1.1).
*   Create a lesson for Student A (e.g., Lesson Management TC1.2) so their balance is negative (e.g., -25.00 USD).

#### **1. Create New Payment**

*   **TC1.1: Create a valid payment that covers a debt**
    *   **Steps:**
        1.  Navigate to the home page (`/`).
        2.  Click the "Payments" button.
        3.  On the payment list page, click "Add New Payment".
        4.  Fill in the form with valid data:
            *   Payment Date: Today's date.
            *   Student: Select "Student A".
            *   Amount: `30.00`
            *   Currency: `USD`
            *   Payment Method: `CARD`
        5.  Click "Add Payment".
    *   **Expected Result:**
        *   Redirects to the payment list page (`/payments/list`).
        *   The new payment is visible in the list.
        *   **Verification (Student Profile):** Navigate to Student A's profile. Their balance for USD should be updated to `5.00` (-25.00 + 30.00).
        *   **Verification (Lesson):** The previously UNPAID lesson for Student A should now have the status `PAID`.

*   **TC1.2: Attempt to create a payment with missing required fields**
    *   **Steps:**
        1.  Navigate to the "Add New Payment" form.
        2.  Do not select a Student.
        3.  Leave the Amount field empty.
        4.  Click "Add Payment".
    *   **Expected Result:**
        *   The form is re-displayed with validation error messages.
        *   An error message like "Please select a student" is shown.
        *   An error message like "Amount is mandatory" is shown.
        *   The payment is not created.

*   **TC1.3: Attempt to create a payment with an invalid amount (zero or negative)**
    *   **Steps:**
        1.  Navigate to the "Add New Payment" form.
        2.  Select "Student A".
        3.  Enter `0.00` for the Amount.
        4.  Click "Add Payment".
    *   **Expected Result:**
        *   The form is re-displayed with a validation error message like "Amount must be greater than 0.01".
        *   The payment is not created.
        *   Repeat with a negative amount (e.g., `-10.00`) and expect the same result.

#### **2. View Payment List and Profile**

*   **TC2.1: View the list of all payments**
    *   **Steps:**
        1.  Ensure at least one payment exists (e.g., from TC1.1).
        2.  Navigate to `/payments/list`.
    *   **Expected Result:**
        *   The page displays a table containing all created payments with correct details (Date, Student, Amount, etc.).

*   **TC2.2: View the profile of a payment**
    *   **Steps:**
        1.  From the payment list, click on a payment's row or an "Details" link.
    *   **Expected Result:**
        *   Redirects to the payment profile page (`/payments/profile/{id}`).
        *   All details of the specific payment are displayed correctly.

#### **3. Edit Payment**

*   **TC3.1: Edit the amount of an existing payment**
    *   **Steps:**
        1.  Create a payment for Student A of `30.00` USD (Balance is `5.00`).
        2.  Navigate to the payment's profile page and click "Edit Payment".
        3.  Change the Amount from `30.00` to `20.00`.
        4.  Click "Update Payment".
    *   **Expected Result:**
        *   Redirects back to the payment's profile page.
        *   The amount on the profile is now `20.00`.
        *   **Verification (Student Profile):** Navigate to Student A's profile. Their balance should be updated to `-5.00` (Original balance of `5.00` is adjusted: `5.00 - (30.00 - 20.00) = -5.00`). The previously PAID lesson should now be UNPAID.

#### **4. Delete Payment**

*   **TC4.1: Delete an existing payment**
    *   **Steps:**
        1.  Create a payment for Student A of `30.00` USD (Balance is `5.00`).
        2.  Navigate to the payment's profile page.
        3.  Click the "Delete Payment" button.
        4.  Confirm the action.
    *   **Expected Result:**
        *   Redirects to the payment list page (`/payments/list`).
        *   The payment is no longer in the list.
        *   **Verification (Student Profile):** Navigate to Student A's profile. Their balance should be reversed to its state before the payment, `-25.00` (`5.00 - 30.00`).
