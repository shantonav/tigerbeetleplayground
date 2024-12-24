package com.drc.poc.drcdemo.tbstorage.api.account;


import com.drc.poc.drcdemo.tbstorage.service.StorageService;
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
    private final StorageService storageService;

    public AccountsApi(StorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping
    List<AccountCreated> createBankAccounts(@RequestBody List<Long> accountIds) {
        return storageService.createBankAccounts(accountIds);
    }

    @PostMapping
    List<AccountCreated> createAccountResults(@RequestBody List<Long> accountIds) {
        return storageService.createAccounts(accountIds);
    }

    @GetMapping
    List<LookupAccountResult> getAccountResults(@RequestBody List<Long> accountIds) {
        return storageService.lookupAccount(accountIds);
    }

    @GetMapping("/overview")
    List<AccountOverview> getAccountsOverview(@RequestBody List<Long> accountIds) {
        return storageService.lookupAccountOverview(accountIds);
    }

}
