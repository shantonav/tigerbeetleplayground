package com.drc.poc.drcdemo.tbstorage.config;

import com.drc.poc.drcdemo.entities.Currency;
import com.drc.poc.drcdemo.tbstorage.service.AccountToCreate;
import com.drc.poc.drcdemo.tbstorage.service.LedgerStorageService;
import com.drc.poc.drcdemo.tbstorage.service.model.AccountCreated;
import com.drc.poc.drcdemo.tbstorage.service.model.LookupAccountResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class LedgerSetup implements ApplicationListener<ContextRefreshedEvent> {
    public static Long DEFAULT_EUR_BANK_ACCOUNT_ID = Currency.EUR.getValue();
    public static Long DEFAULT_DRC_BANK_ACCOUNT_ID = Currency.DRC.getValue();;
    private final LedgerStorageService ledgerStorageService;

    public LedgerSetup(LedgerStorageService ledgerStorageService) {
        this.ledgerStorageService = ledgerStorageService;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ledgerStorageService.createBankAccounts(List.of(
                new AccountToCreate(DEFAULT_EUR_BANK_ACCOUNT_ID, (int) Currency.EUR.getValue()),
                new AccountToCreate(DEFAULT_DRC_BANK_ACCOUNT_ID, (int) Currency.DRC.getValue()))
        );

    }
}