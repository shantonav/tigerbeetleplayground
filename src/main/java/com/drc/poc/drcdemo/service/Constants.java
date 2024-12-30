package com.drc.poc.drcdemo.service;

import org.springframework.util.ObjectUtils;

import java.util.function.Function;

public interface Constants {
    Function<String, Long> convertAccountNumber = accountNumberStr -> {
        Long lAccountNumber = -1L;
        if (!ObjectUtils.isEmpty(accountNumberStr)) {
            lAccountNumber = Long.parseLong(accountNumberStr);
        }
        return lAccountNumber;
    };

}
