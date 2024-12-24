package com.drc.poc.drcdemo.tbstorage.service.model;

import java.math.BigInteger;

public record DepositRequest(Long creditId,
                             BigInteger amountInCents) {}
