package com.art.tutordesk.payment;

import lombok.Getter;

@Getter
public enum Currency {
    USD("$"),
    EUR("€"),
    UAH("₴"),
    PLN("zł");

    private final String symbol;

    Currency(String symbol) {
        this.symbol = symbol;
    }

}
