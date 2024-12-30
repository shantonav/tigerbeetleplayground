package com.drc.poc.drcdemo.tbstorage.api.account;


import com.drc.poc.drcdemo.tbstorage.service.AccountToCreate;
import com.drc.poc.drcdemo.tbstorage.service.LedgerStorageService;
import com.drc.poc.drcdemo.tbstorage.service.model.AccountCreated;
import com.drc.poc.drcdemo.tbstorage.service.model.AccountOverview;
import com.drc.poc.drcdemo.tbstorage.service.model.LookupAccountResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/accounts")
public class AccountsApi {
    private final LedgerStorageService ledgerStorageService;

    public AccountsApi(LedgerStorageService ledgerStorageService) {
        this.ledgerStorageService = ledgerStorageService;
    }

    @PostMapping("/bank/create")
    List<AccountCreated> createBankAccounts(@RequestBody List<AccountToCreate> accountIds) {
        return ledgerStorageService.createBankAccounts(accountIds);
    }

    @PostMapping
    List<AccountCreated> createAccountResults(@RequestBody List<AccountToCreate> accountIds) {
        return ledgerStorageService.createAccounts(accountIds);
    }

    @GetMapping
    List<LookupAccountResult> getAccountResults(@RequestBody List<Long> accountIds) {
        return ledgerStorageService.lookupAccounts(accountIds);
    }

    @GetMapping("/overview")
    List<AccountOverview> getAccountsOverview(@RequestBody List<Long> accountIds) {
        return ledgerStorageService.lookupAccountsOverview(accountIds);
    }

}
