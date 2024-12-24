package com.drc.poc.drcdemo.tbstorage.service.model;

import java.math.BigInteger;
import java.time.OffsetDateTime;

public record LookupAccountResult(Long accountId,
                                  BigInteger debitsPending,
                                  BigInteger debitsPosted,
                                  BigInteger creditsPending,
                                  BigInteger creditsPosted,
                                  Integer ledger,
                                  Integer code,
                                  Integer flags ,
                                  OffsetDateTime timestamp) {
}
