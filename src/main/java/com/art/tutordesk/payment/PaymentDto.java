package com.art.tutordesk.payment;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class PaymentDto {
    private Long id;

    @NotNull(message = "Payment date is mandatory")
    private LocalDate paymentDate;

    @NotNull(message = "Student is mandatory")
    private Long studentId;

    private String studentFirstName;
    private String studentLastName;

    @NotNull(message = "Payment method is mandatory")
    private PaymentMethod paymentMethod;

    @NotNull(message = "Amount is mandatory")
    @DecimalMin(value = "0.01", message = "Amount must be positive")
    private BigDecimal amount;

    @NotNull(message = "Currency is mandatory")
    private Currency currency;
}