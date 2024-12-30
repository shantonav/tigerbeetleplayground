package com.drc.poc.drcdemo.service;

import com.drc.poc.drcdemo.dtos.AccountDto;
import com.drc.poc.drcdemo.tbstorage.service.model.TransferResult;

public interface TransferServiceInterface {
    TransferResult deposit(Long amount, AccountDto accountDto);
    TransferResult withDraw(Long amount, AccountDto accountDto);
    TransferResult transferFunds(AccountDetails fromAccountDetails, AccountDetails toAccountDetails,
                                 Long amount,
                                 String fromAccountName,
                                 String fromAccountNumber,
                                 String toAccountName,
                                 String toAccountNumber);

}
