package com.art.tutordesk.student;

import com.art.tutordesk.payment.Currency;
import com.art.tutordesk.student.validation.AtLeastOnePriceNotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@AtLeastOnePriceNotNull
public class StudentDto {
    private Long id;

    @NotBlank(message = "First name is mandatory")
    @Size(max = 50, message = "First name cannot exceed 50 characters")
    private String firstName;

    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    private String lastName;

    @Size(max = 50, message = "Knowledge level cannot exceed 50 characters")
    private String knowledgeLevel; // "A2", "B1", "C1"

    @Min(0)
    private Integer age;

    @DecimalMin(value = "0.00", message = "Individual price must be non-negative")
    private BigDecimal priceIndividual;

    @DecimalMin(value = "0.00", message = "Group price must be non-negative")
    private BigDecimal priceGroup;

    @NotNull(message = "Currency is mandatory")
    private Currency currency;

    private boolean active = true;

    private Map<Currency, BigDecimal> balances;
}
