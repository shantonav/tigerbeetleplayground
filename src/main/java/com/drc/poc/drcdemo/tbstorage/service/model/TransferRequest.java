package com.drc.poc.drcdemo.tbstorage.service.model;

import com.drc.poc.drcdemo.entities.Currency;

import java.math.BigInteger;

public record TransferRequest(
        Long debitId,
        Long creditId,
        BigInteger amountInCents,
        Currency currency, Integer flags) {
}
