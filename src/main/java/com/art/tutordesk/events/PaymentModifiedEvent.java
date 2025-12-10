package com.art.tutordesk.events;

import com.art.tutordesk.payment.Payment;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Getter
@RequiredArgsConstructor
public class PaymentModifiedEvent {
    private final Payment payment;
    private final BigDecimal oldPaymentAmount;
}
