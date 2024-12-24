package com.drc.poc.drcdemo.dtos;

import com.drc.poc.drcdemo.service.AccountDetails;

public record TransferDto(Long amount, String fromAccountName, AccountDetails fromAccountNumber,
                          String toAccountName, AccountDetails toAccountNumber) {
}
