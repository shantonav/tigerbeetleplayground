package com.drc.poc.drcdemo.tbstorage.service.model;

import java.math.BigInteger;

public record TransferRequest(
        Long debitId,
        Long creditId,
        BigInteger amountInCents
) {
}
