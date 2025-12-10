package com.art.tutordesk.events;

import com.art.tutordesk.payment.Payment;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PaymentCreatedEvent {
    private final Payment payment;
}
