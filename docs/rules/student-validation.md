# Student DTO Validation Rules

This document outlines the validation rules applied to the `StudentDto` class, which is used for creating and updating student records. The rules are enforced by Hibernate Validator through annotations in the source code.

---

### **Validation Rules per Field:**

*   **`firstName`**
    *   **Required:** Yes
    *   **Type:** `String`
    *   **Rules:**
        *   Must not be blank (`@NotBlank`).
        *   Maximum length is 50 characters (`@Size(max = 50)`).

*   **`lastName`**
    *   **Required:** No
    *   **Type:** `String`
    *   **Rules:**
        *   Maximum length is 50 characters (`@Size(max = 50)`).

*   **`age`**
    *   **Required:** No
    *   **Type:** `Integer`
    *   **Rules:**
        *   If provided, must be a non-negative number (≥ 0) (`@Min(0)`).

*   **`knowledgeLevel`**
    *   **Required:** No
    *   **Type:** `String`
    *   **Rules:**
        *   Maximum length is 50 characters (`@Size(max = 50)`).

*   **`country`**
    *   **Required:** No
    *   **Type:** `String`
    *   **Rules:**
        *   Maximum length is 50 characters (`@Size(max = 50)`).

*   **`phoneNumber`**
    *   **Required:** No
    *   **Type:** `String`
    *   **Rules:**
        *   Maximum length is 50 characters (`@Size(max = 50)`). No specific format is enforced.

*   **`globalGoal`**
    *   **Required:** No
    *   **Type:** `String` (long text)
    *   **Rules:**
        *   Maximum length is 500 characters (`@Size(max = 500)`).

*   **`priceIndividual`**
    *   **Required:** Yes
    *   **Type:** `BigDecimal`
    *   **Rules:**
        *   Must not be null (`@NotNull`).
        *   Must be a non-negative value (≥ 0.00) (`@DecimalMin("0.00")`).

*   **`priceGroup`**
    *   **Required:** Yes
    *   **Type:** `BigDecimal`
    *   **Rules:**
        *   Must not be null (`@NotNull`).
        *   Must be a non-negative value (≥ 0.00) (`@DecimalMin("0.00")`).

*   **`currency`**
    *   **Required:** Yes
    *   **Type:** `Enum`
    *   **Rules:**
        *   Must not be null (`@NotNull`). A value must be selected from the predefined list of currencies.