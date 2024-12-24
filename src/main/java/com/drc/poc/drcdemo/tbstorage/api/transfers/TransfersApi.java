package com.drc.poc.drcdemo.tbstorage.api.transfers;


import com.drc.poc.drcdemo.tbstorage.service.LedgerStorageService;
import com.drc.poc.drcdemo.tbstorage.service.model.BatchDepositRequest;
import com.drc.poc.drcdemo.tbstorage.service.model.BatchWithdrawRequest;
import com.drc.poc.drcdemo.tbstorage.service.model.TransferOverview;
import com.drc.poc.drcdemo.tbstorage.service.model.TransferRequest;
import com.drc.poc.drcdemo.tbstorage.service.model.TransferResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/transfers")
public class TransfersApi {
    private final LedgerStorageService ledgerStorageService;

    public TransfersApi(LedgerStorageService ledgerStorageService) {
        this.ledgerStorageService = ledgerStorageService;
    }

    @PostMapping
    List<TransferResult> transfer(@RequestBody List<TransferRequest> transferRequests) {
        return ledgerStorageService.createTransfers(transferRequests);
    }

    @PostMapping("/deposit")
    List<TransferResult> deposit(@RequestBody BatchDepositRequest batchDepositRequest) {
        return ledgerStorageService.deposit(batchDepositRequest);
    }

    @PostMapping("/withdraw")
    List<TransferResult> withdraw(@RequestBody BatchWithdrawRequest batchWithdrawRequest) {
        return ledgerStorageService.withdraw(batchWithdrawRequest);
    }

    @GetMapping("/overview")
    List<TransferOverview> getTransferOverview(@RequestBody List<Long> accountIds) {
        return ledgerStorageService.lookupTransfers(accountIds);
    }
}
