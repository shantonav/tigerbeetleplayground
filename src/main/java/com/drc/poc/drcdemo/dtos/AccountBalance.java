package com.drc.poc.drcdemo.dtos;

import java.math.BigInteger;

public record AccountBalance(String accountName, Long accountId, BigInteger balance,
                             com.drc.poc.drcdemo.entities.Currency currency) {
}
