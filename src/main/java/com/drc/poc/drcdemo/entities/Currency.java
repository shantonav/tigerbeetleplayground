package com.drc.poc.drcdemo.entities;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum Currency {
    EUR(1, 1L),
    USD(3, 1L),
    CDF(2, 10L);
    private final long value;
    private final long rate;
    Currency(long value, long conversionRate) {
        this.value = value;
        this.rate = conversionRate;
    }

    public static Currency getCurrencyByValue(long value) {
        return Arrays.stream(values())
                .filter(currency -> currency.value == value)
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
