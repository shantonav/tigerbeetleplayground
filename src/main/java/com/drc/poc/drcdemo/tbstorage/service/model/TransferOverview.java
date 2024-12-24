package com.drc.poc.drcdemo.tbstorage.service.model;

import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TransferOverview(
        UUID transferId,
        Long debitId,
        Long creditId,
        BigInteger amount,
        UUID pendingId,
        Integer timeout,
        Integer ledger,
        Integer code,
        Integer flags,
        OffsetDateTime timestamp
) {
}
