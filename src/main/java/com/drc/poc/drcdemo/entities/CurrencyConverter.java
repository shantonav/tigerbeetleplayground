package com.drc.poc.drcdemo.entities;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.util.ObjectUtils;

import java.util.Arrays;

@Converter(autoApply = true)
public class CurrencyConverter implements AttributeConverter<Currency, String> {
    @Override
    public String convertToDatabaseColumn(Currency currency) {
        return currency.name();
    }

    @Override
    public Currency convertToEntityAttribute(String currencyStr) {
        return ObjectUtils.isEmpty(currencyStr) ? null :
                Arrays.stream(Currency.values()).filter(e -> e.name().equals(currencyStr)).findFirst().orElse(null);
    }
}
