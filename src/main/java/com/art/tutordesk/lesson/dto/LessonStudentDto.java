package com.art.tutordesk.lesson.dto;

import com.art.tutordesk.lesson.PaymentStatus;
import com.art.tutordesk.payment.Currency;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class LessonStudentDto {
    private Long id; // ID of the LessonStudent association
    private Long studentId;
    private String studentFirstName;
    private String studentLastName;
    private PaymentStatus paymentStatus;
    private BigDecimal price;
    private Currency currency;
}
