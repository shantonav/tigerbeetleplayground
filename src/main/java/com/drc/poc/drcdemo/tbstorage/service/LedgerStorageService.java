package com.drc.poc.drcdemo.tbstorage.service;

import com.drc.poc.drcdemo.tbstorage.service.model.AccountCreated;
import com.drc.poc.drcdemo.tbstorage.service.model.AccountOverview;
import com.drc.poc.drcdemo.tbstorage.service.model.BatchDepositRequest;
import com.drc.poc.drcdemo.tbstorage.service.model.BatchWithdrawRequest;
import com.drc.poc.drcdemo.tbstorage.service.model.LookupAccountResult;
import com.drc.poc.drcdemo.tbstorage.service.model.TransferOverview;
import com.drc.poc.drcdemo.tbstorage.service.model.TransferRequest;
import com.drc.poc.drcdemo.tbstorage.service.model.TransferResult;
import com.drc.poc.drcdemo.tbstorage.service.model.TransferType;
import com.tigerbeetle.AccountBatch;
import com.tigerbeetle.AccountFilter;
import com.tigerbeetle.AccountFlags;
import com.tigerbeetle.Client;
import com.tigerbeetle.CreateAccountResult;
import com.tigerbeetle.CreateTransferResult;
import com.tigerbeetle.CreateTransferResultBatch;
import com.tigerbeetle.IdBatch;
import com.tigerbeetle.TransferBatch;
import com.tigerbeetle.UInt128;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

@Service
public class LedgerStorageService {
    private final Client tigerBeetleClient;

    public LedgerStorageService(Client tigerBeetleClient) {
        this.tigerBeetleClient = tigerBeetleClient;
    }

    public List<AccountCreated> createBankAccounts(List<Long> accountIds) {
        return createAccounts(accountIds, AccountFlags.NONE);
    }

    public List<AccountCreated> createAccounts(List<Long> accountIds) {
        return createAccounts(accountIds, AccountFlags.DEBITS_MUST_NOT_EXCEED_CREDITS);
    }

    private List<AccountCreated> createAccounts(List<Long> accountIds, int flags) {
        if (accountIds.isEmpty()) {
            return emptyList();
        }

        var result = new ArrayList<AccountCreated>();

        var accountBatch = new AccountBatch(accountIds.size());
        var indexedCreateAccountResult = new HashMap<Integer, MutablePair<Long, CreateAccountResult>>();
        IntStream.range(0, accountIds.size()).forEach(index -> {
            long accountId = accountIds.get(index);
            MutablePair<Long, CreateAccountResult> longCreateAccountResultHashMap = new MutablePair<>(accountId, CreateAccountResult.Ok);
            indexedCreateAccountResult.put(index, longCreateAccountResultHashMap);

            accountBatch.add();
            accountBatch.setId(UInt128.asBytes(accountId));
            accountBatch.setLedger(1);
            accountBatch.setCode(1);
            accountBatch.setFlags(flags);
        });

        var createAccountResultBatch = tigerBeetleClient.createAccounts(accountBatch);

        while(createAccountResultBatch.next()) {
            MutablePair<Long, CreateAccountResult> resultMutablePair =
                    indexedCreateAccountResult.get(createAccountResultBatch.getIndex());

            resultMutablePair.setRight(createAccountResultBatch.getResult());
        }

        indexedCreateAccountResult.values().forEach(accountResult -> {
            result.add(new AccountCreated(accountResult.left, accountResult.right.value, accountResult.right.name()));
        });

        return result;

    }

    public List<LookupAccountResult> lookupAccount(List<Long> accountIds) {
        if (accountIds.isEmpty()) {
            return emptyList();
        }

        var idBatch = new IdBatch(accountIds.size());

        accountIds.forEach(idBatch::add);

        AccountBatch accountBatch = tigerBeetleClient.lookupAccounts(idBatch);
        var result = new ArrayList<LookupAccountResult>();

        while (accountBatch.next()) {
            var lookupAccountResult = new LookupAccountResult(
                    UInt128.asLong(accountBatch.getId(), UInt128.LeastSignificant),
                    accountBatch.getDebitsPending(),
                    accountBatch.getDebitsPosted(),
                    accountBatch.getCreditsPending(),
                    accountBatch.getCreditsPosted(),
                    accountBatch.getLedger(),
                    accountBatch.getCode(),
                    accountBatch.getFlags(),
                    OffsetDateTime.ofInstant(Instant.ofEpochMilli(accountBatch.getTimestamp()), ZoneOffset.UTC)
            );
            result.add(lookupAccountResult);
        }

        return result;

    }

    public List<AccountOverview> lookupAccountOverview(List<Long> accountIds) {
        if (accountIds.isEmpty()) {
            return emptyList();
        }

        List<LookupAccountResult> lookupAccountResults = lookupAccount(accountIds);

        return lookupAccountResults.stream()
               .map(lookupAccountResult -> {
                   List<TransferOverview> transferOverviews = lookupTransfers(singletonList(lookupAccountResult.accountId()));
                   return new AccountOverview(lookupAccountResult, transferOverviews);
               })
               .toList();
    }

    public List<TransferResult> createTransfers(List<TransferRequest> transferRequests)  {
        if(transferRequests.isEmpty()) {
            return emptyList();
        }

        TransferBatch transferBatch = new TransferBatch(transferRequests.size());
        Map<Integer, MutablePair<UUID, CreateTransferResult>> transferResultArray = new HashMap<>();

        IntStream.range(0, transferRequests.size()).forEach(index -> {
            TransferRequest transferRequest = transferRequests.get(index);
            UUID transferId = UUID.randomUUID();

            transferBatch.add();
            transferBatch.setId(UInt128.asBytes(transferId));
            transferBatch.setLedger(1);
            transferBatch.setCode(1);
            transferBatch.setDebitAccountId(UInt128.asBytes(transferRequest.debitId()));
            transferBatch.setCreditAccountId(UInt128.asBytes(transferRequest.creditId()));
            transferBatch.setAmount(transferRequest.amountInCents());
            transferBatch.setFlags(TransferType.POST_DIRECT.getValue());

            transferResultArray.put(index, new MutablePair<>(transferId, CreateTransferResult.Ok));
        });

        CreateTransferResultBatch transferResults = tigerBeetleClient.createTransfers(transferBatch);

        while (transferResults.next()) {
            MutablePair<UUID, CreateTransferResult> result = transferResultArray.get(transferResults.getIndex());
            if (result != null) {
                result.setRight(transferResults.getResult());
            }
        }

        return transferResultArray.values().stream()
                .map(pair -> new TransferResult(pair.getLeft(), pair.getRight().value, pair.getRight().name()))
                .collect(Collectors.toList());
    }

    public List<TransferResult> deposit(BatchDepositRequest batchDepositRequest) {
        Long debitBankLedgerAccountId = batchDepositRequest.bankAccountId();
        List<TransferRequest> depositTransferRequests = batchDepositRequest
                .depositRequests()
                .stream()
                .map(depositRequest -> new TransferRequest(
                        debitBankLedgerAccountId, depositRequest.creditId(), depositRequest.amountInCents())).toList();

        return createTransfers(depositTransferRequests);
    }

    public List<TransferResult> withdraw(BatchWithdrawRequest batchWithdrawRequest) {
        Long creditAccountIt = batchWithdrawRequest.accountWithdrawId();
        List<TransferRequest> depositTransferRequests = batchWithdrawRequest
                .withdrawRequests()
                .stream()
                .map(withdrawRequest -> new TransferRequest(
                        withdrawRequest.debitId(), creditAccountIt, withdrawRequest.amountInCents())).toList();

        return createTransfers(depositTransferRequests);
    }

    public List<TransferOverview> lookupTransfers(List<Long> accountIds) {
        if (accountIds.isEmpty()) {
            return emptyList();
        }

        var result = new ArrayList<TransferOverview>();
        accountIds.forEach(accountId -> {
            var accountFilter = getAccountFilter(accountId);

            var transferBatch = tigerBeetleClient.getAccountTransfers(accountFilter);
            while (transferBatch.next()) {

                var debitLedgerId = UInt128.asLong(transferBatch.getDebitAccountId(), UInt128.LeastSignificant);
                var creditLedgerId = UInt128.asLong(transferBatch.getCreditAccountId(), UInt128.LeastSignificant);

                result.add(
                        new TransferOverview(
                                UInt128.asUUID(transferBatch.getId()),
                                debitLedgerId,
                                creditLedgerId,
                                transferBatch.getAmount(),
                                UInt128.asUUID(transferBatch.getPendingId()),
                                transferBatch.getTimeout(),
                                transferBatch.getLedger(),
                                transferBatch.getCode(),
                                transferBatch.getFlags(),
                                OffsetDateTime.ofInstant(Instant.ofEpochMilli(transferBatch.getTimestamp()), ZoneOffset.UTC)
                        )
                );
            }

        });
        return result;
    }

    private AccountFilter getAccountFilter(Long accountId) {
        var accountFilter = new AccountFilter();
        accountFilter.setAccountId(accountId);

        accountFilter.setTimestampMin(0); // No filter by Timestamp.
        accountFilter.setTimestampMax(0); // No filter by Timestamp.
        accountFilter.setLimit(8); // Limit transfers at most.
        accountFilter.setDebits(true); // Include transfer from the debit side.
        accountFilter.setCredits(true); // Include transfer from the credit side.
        accountFilter.setReversed(true); // Sort by timestamp in reverse-chronological order.
        return accountFilter;
    }


}
