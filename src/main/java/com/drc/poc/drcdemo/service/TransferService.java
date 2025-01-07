package com.drc.poc.drcdemo.service;


import com.drc.poc.drcdemo.dtos.AccountDto;
import com.drc.poc.drcdemo.dtos.IndividualDto;
import com.drc.poc.drcdemo.tbstorage.service.LedgerStorageService;
import com.drc.poc.drcdemo.tbstorage.service.model.*;
import com.tigerbeetle.TransferFlags;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static  com.drc.poc.drcdemo.service.Constants.*;

@Service
@AllArgsConstructor
@Slf4j
public class TransferService implements TransferServiceInterface{
    private final LedgerStorageService ledgerStorageService;
    private final AccountService accountService;

    @Override
    public TransferResult deposit(Long amount, AccountDto accountDto) {
        DepositRequest depositRequest = new DepositRequest(accountDto.getAccountNumber(), BigInteger.valueOf(amount));
        BatchDepositRequest batchDepositRequest = new BatchDepositRequest(accountDto.getCurrency().getValue(), Collections.singletonList(depositRequest), accountDto.getCurrency());

        return ledgerStorageService.deposit(batchDepositRequest).get(0);
    }

    @Override
    public TransferResult withDraw(Long amount, AccountDto accountDto) {
        WithdrawRequest withdrawRequest = new WithdrawRequest(accountDto.getAccountNumber(), BigInteger.valueOf(amount));
        BatchWithdrawRequest batchWithdrawRequest = new BatchWithdrawRequest( accountDto.getCurrency().getValue(), Collections.singletonList(withdrawRequest), accountDto.getCurrency());
        return ledgerStorageService.withdraw(batchWithdrawRequest).get(0);
    }

    @Override
    public TransferResult transferFunds(AccountDetails fromAccountDetails, AccountDetails toAccountDetails,
                                        Long amount,
                                        String fromAccountName,
                                        String fromAccountNumber,
                                        String toAccountName,
                                        String toAccountNumber){
        Pair<AtomicReference<AccountDto>, AtomicReference<AccountDto>> fromAndToPair =
                resolveTransferAccount(fromAccountDetails, toAccountDetails, fromAccountName, fromAccountNumber, toAccountName, toAccountNumber);

        if ( fromAndToPair.getLeft() == null || fromAndToPair.getRight() == null ) {
            throw new RuntimeException("Transfer aborted as account(s) cannot be resolved");
        }

        final AtomicReference<AccountDto> fromIndividualDtoRef = fromAndToPair.getLeft();
        final AtomicReference<AccountDto>  toIndividualDtoRef = fromAndToPair.getRight();

        TransferResult transfersResult = transferFunds(fromIndividualDtoRef, toIndividualDtoRef, amount, fromAccountName, fromAccountNumber, toAccountName, toAccountNumber );

        if (transfersResult.response() != 0) {
            log.error("Transfer failed from {}-{} to  {}-{}, caused by: {}", fromAccountName, fromAccountNumber, toAccountName, toAccountNumber, transfersResult.description());
            throw new RuntimeException("Transfer failed");
        }

        return transfersResult;
    }

    private TransferResult transferFunds(AtomicReference<AccountDto> fromIndividualDtoRef, AtomicReference<AccountDto>  toIndividualDtoRef, Long amount,
                                         String fromAccountName,
                                         String fromAccountNumber,
                                         String toAccountName,
                                         String toAccountNumber){
        if ( !fromIndividualDtoRef.get().getCurrency().equals(toIndividualDtoRef.get().getCurrency()) ){

            BigInteger targetAmount = BigInteger.valueOf(amount * toIndividualDtoRef.get().getCurrency().getRate());
            List<TransferRequest> transferRequests = List.of(
                    new TransferRequest(
                            fromIndividualDtoRef.get().getAccountNumber(), fromIndividualDtoRef.get().getCurrency().getValue(), BigInteger.valueOf(amount), fromIndividualDtoRef.get().getCurrency(), TransferFlags.LINKED),
                    new TransferRequest(
                            toIndividualDtoRef.get().getCurrency().getValue(), toIndividualDtoRef.get().getAccountNumber(), targetAmount, toIndividualDtoRef.get().getCurrency(), null)
            );
            log.info("\n Source money {} to target money {} is transferred from account name {} or number {}, " +
                    "to account name {} or number {}", amount, targetAmount, fromAccountName, fromAccountNumber, toAccountName, toAccountNumber);
            return ledgerStorageService.createTransfers(transferRequests).get(0);

        }

        TransferRequest transferRequest = new TransferRequest(fromIndividualDtoRef.get().getAccountNumber(), toIndividualDtoRef.get().getAccountNumber(), BigInteger.valueOf(amount), fromIndividualDtoRef.getOpaque().getCurrency(), null);

        log.info("\n Money {} is transferred from account name {} or number {}, " +
                "to account name {} or number {}", amount, fromAccountName, fromAccountNumber, toAccountName, toAccountNumber);
        return ledgerStorageService.createTransfers(Collections.singletonList(transferRequest)).get(0);

    }

    private Pair<AtomicReference<AccountDto>, AtomicReference<AccountDto>> resolveTransferAccount(AccountDetails fromAccountDetails, AccountDetails toAccountDetails,
                                                                                                  String fromAccountName,
                                                                                                  String fromAccountNumber,
                                                                                                  String toAccountName,
                                                                                                  String toAccountNumber) {
        final AtomicReference<AccountDto> toIndividualDtoRef = new AtomicReference<>();
        final AtomicReference<AccountDto> fromIndividualDtoRef = new AtomicReference<>();

        if ( fromAccountDetails.accountType().equals(AccountType.GROUP) && toAccountDetails.accountType().equals(AccountType.GROUP)) {
            log.error("Cannot transfer between groups");
            return Pair.of(null, null);
        } else if ( fromAccountDetails.accountType().equals(AccountType.GROUP)) {
            Optional<IndividualDto> toIndividualDto = accountService.findIndividual(toAccountName, convertAccountNumber.apply(toAccountNumber));
            if (toIndividualDto.isEmpty()) {
                log.error("To account name {} and/or number {} does not exist. Transfer aborted ", toAccountName, toAccountNumber);
                return Pair.of(null, null);
            }
            if ( toIndividualDto.get().getGroups().stream().noneMatch(grp -> grp.getGroupName().equals(fromAccountName) ||
                    grp.getAccountNumber().equals(convertAccountNumber.apply(toAccountNumber)))) {
                log.error("To account name {} or number {} does not belong to group account name {} or number {}, transfer aborted",
                        toAccountName, toAccountNumber, fromAccountName, fromAccountNumber);
                return Pair.of(null, null);
            }
            fromIndividualDtoRef.set(accountService.findGroup(fromAccountName, convertAccountNumber.apply(fromAccountNumber))
                    .orElseThrow(() -> new RuntimeException("Group could not be found, something is buggy in code")));
            toIndividualDtoRef.set(toIndividualDto.orElseThrow(() -> new RuntimeException("Individual could not be found, something is wrong in code")));

        }else if ( toAccountDetails.accountType().equals(AccountType.GROUP)) {
            Optional<IndividualDto> fromIndividualDto = accountService.findIndividual(fromAccountName, convertAccountNumber.apply(fromAccountNumber));
            if (fromIndividualDto.isEmpty()) {
                log.error("From account name {} and/or number {} does not exist. Transfer aborted ", fromAccountName, fromAccountNumber);
                return Pair.of(null, null);
            }
            if ( fromIndividualDto.get().getGroups().stream().noneMatch(grp -> grp.getGroupName().equals(toAccountName) ||
                    grp.getAccountNumber().equals(convertAccountNumber.apply(toAccountNumber)))) {
                log.error("From Account name {} or number {} does not belong to group account name {} or number {}, transfer aborted",
                        fromAccountName, fromAccountNumber, toAccountName, toAccountNumber);
                return Pair.of(null, null);
            }

            toIndividualDtoRef.set(accountService.findGroup(toAccountName, convertAccountNumber.apply(toAccountNumber))
                    .orElseThrow(() -> new RuntimeException("Group could not be found, something is buggy in code")));
            fromIndividualDtoRef.set(fromIndividualDto.orElseThrow(() -> new RuntimeException("Individual could not be found, something is wrong in code")));

        }else{

            fromIndividualDtoRef.set(accountService.findIndividual(fromAccountName, convertAccountNumber.apply(fromAccountNumber))
                    .orElseThrow(() -> new RuntimeException("From account reference not found, something smelling in code.")));
            toIndividualDtoRef.set(accountService.findIndividual(toAccountName, convertAccountNumber.apply(toAccountNumber))
                    .orElseThrow(() -> new RuntimeException("To account reference not found, something smelling in code.")));
        }

        return Pair.of(fromIndividualDtoRef, toIndividualDtoRef);
    }
}
