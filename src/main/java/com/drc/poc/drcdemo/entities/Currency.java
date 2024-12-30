package com.drc.poc.drcdemo.entities;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum Currency {
    EUR(1, 1.0d),
    USD(3, 0.94d),
    DRC(2, 10.0d);
    private final int value;
    private final double rate;
    Currency(int value, double conversionRate) {
        this.value = value;
        this.rate = conversionRate;
    }

    public static Currency getCurrencyByValue(int value) {
        return Arrays.stream(values())
                .filter(currency -> currency.value == value)
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
