package com.art.tutordesk.events;

import com.art.tutordesk.payment.Payment;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PaymentDeletedEvent {
    private final Payment payment;
}
