package com.drc.poc.drcdemo.tbstorage.config;

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
    public static Long DEFAULT_BANK_ACCOUNT_ID = 1L;
    private final LedgerStorageService ledgerStorageService;

    public LedgerSetup(LedgerStorageService ledgerStorageService) {
        this.ledgerStorageService = ledgerStorageService;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        List<LookupAccountResult> lookupAccountResults = ledgerStorageService.lookupAccounts(Collections.singletonList(DEFAULT_BANK_ACCOUNT_ID));
        if (lookupAccountResults.isEmpty()) {

            List<AccountCreated> bankAccounts = ledgerStorageService.createBankAccounts(Collections.singletonList(DEFAULT_BANK_ACCOUNT_ID));

            if(bankAccounts.isEmpty()) {
                log.error("Bank accounts not created");
                throw new RuntimeException("Bank accounts not created");
            }
        }


    }
}