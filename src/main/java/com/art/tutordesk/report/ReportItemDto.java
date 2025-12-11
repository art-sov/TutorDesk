package com.art.tutordesk.report;

import com.art.tutordesk.payment.Currency;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class ReportItemDto {
    private String studentName;
    private ItemType itemType;
    private Currency currency;
    private BigDecimal amount;
    private LocalDate date;

    public enum ItemType {
        LESSON, PAYMENT
    }
}
