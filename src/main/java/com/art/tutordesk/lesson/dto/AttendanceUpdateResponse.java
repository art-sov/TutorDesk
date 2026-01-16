package com.art.tutordesk.lesson.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class AttendanceUpdateResponse {
    private BigDecimal newPrice;
    private String newPaymentStatus;
}
