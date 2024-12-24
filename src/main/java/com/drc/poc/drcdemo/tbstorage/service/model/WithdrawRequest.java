package com.drc.poc.drcdemo.tbstorage.service.model;

import java.math.BigInteger;

public record WithdrawRequest(Long debitId,
                              BigInteger amountInCents) {
}
