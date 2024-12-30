package com.drc.poc.drcdemo.service;

import com.drc.poc.drcdemo.dtos.*;
import com.drc.poc.drcdemo.entities.Currency;
import com.drc.poc.drcdemo.tbstorage.service.LedgerStorageService;
import com.drc.poc.drcdemo.tbstorage.service.model.BatchDepositRequest;
import com.drc.poc.drcdemo.tbstorage.service.model.BatchWithdrawRequest;
import com.drc.poc.drcdemo.tbstorage.service.model.DepositRequest;
import com.drc.poc.drcdemo.tbstorage.service.model.TransferRequest;
import com.drc.poc.drcdemo.tbstorage.service.model.TransferResult;
import com.drc.poc.drcdemo.tbstorage.service.model.WithdrawRequest;
import com.tigerbeetle.TransferFlags;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.util.ObjectUtils;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

@ShellComponent
@Slf4j
@AllArgsConstructor
public class ApplicationCommandReceptors {
    private final AccountServiceInterface accountService;
    private final LedgerStorageService ledgerStorageService;


    @ShellMethod(key = "createAccount", value = "Create an individual account")
    public String createAccount(@ShellOption(value = "--accountHolderName") String accountHolderName, @ShellOption(value = "--currency") Currency currency) {

        IndividualDto individualDto = accountService.createAnAccount(new IndividualDto(currency, accountHolderName));

        return "Created account number " + individualDto.getAccountNumber() + " for " + accountHolderName + " for Currency " + currency;
    }

    @ShellMethod(key = "lookupAllAccounts", value = "Lookup all accounts")
    public String lookupAllAccounts() {

        List<AccountBalance> balanceForAllAccounts = accountService.getBalanceForAllAccounts();

        String printOutResponse = balanceForAllAccounts.stream()
                .map(accountBalance -> String.format("Account name: %s, accountNumber: %d, balance: %d, in Currency:%s",
                        accountBalance.accountName(), accountBalance.accountId(), accountBalance.balance(), accountBalance.currency()))
                .collect(Collectors.joining("\n"));

        return "Account balances: \n" + printOutResponse;
    }

    @ShellMethod(key = "createAGroupAccount", value = "Create a Group account")
    public String createAGroupAccount(@ShellOption(value = "--groupName") String groupName, @ShellOption(value = "--currency") Currency currency) {

        GroupDto individualDto = accountService.createAGroupAccount(new GroupDto(currency, groupName));

        return "Created group account number " + individualDto.getAccountNumber() + " for " + groupName + " for Currency " + currency;
    }

    @ShellMethod(key = "addAccountToGroup", value = "Create a Group account")
    public String addAccountToGroup(@ShellOption(value = "--accountHolderName") String accountHolderName,
                                    @ShellOption(value = "--groupName") String groupName) {

        Boolean individualAdded = accountService.addIndividualToAGroup(new GroupIndividualDto(groupName, accountHolderName));

        return "Individual " + accountHolderName+ " added to group " + groupName + " successfully ? "+ individualAdded;
    }

    private final Function<String, Long> convertAccountNumber = accountNumberStr -> {
        Long lAccountNumber = -1L;
        if (!ObjectUtils.isEmpty(accountNumberStr)) {
            lAccountNumber = Long.parseLong(accountNumberStr);
        }
        return lAccountNumber;
    };

    @ShellMethod(key = "depositToAccount", value = "Deposit money to an account")
    public String depositToAccount(@ShellOption(value = "--amount") Long amount,
                                   @ShellOption(value = "--accountName", defaultValue = "") String accountHolderName,
                                   @ShellOption(value = "--accountNumber", defaultValue = "" ) String accountNumber) {
        Optional<AccountDetails> accountDetails =
                accountService.checkIfAccountExists(new AccountOperationDto(amount, accountHolderName, convertAccountNumber.apply(accountNumber)));
        if (accountDetails.isEmpty()){
            log.error("Account name {} and/or number {} does not exist. Deposit aborted ", accountHolderName, accountNumber);
            return "";
        };
        AccountDto accountDto;
        if (accountDetails.get().accountType().equals(AccountType.GROUP)){
            accountDto =
                    accountService.findGroup(accountHolderName, convertAccountNumber.apply(accountNumber))
                            .orElseThrow(() -> new RuntimeException("Group not found"));

        } else {
            accountDto =
                    accountService.findIndividual(accountHolderName, convertAccountNumber.apply(accountNumber))
                            .orElseThrow(() -> new RuntimeException("Individual not found"));
        }

        DepositRequest depositRequest = new DepositRequest(accountDto.getAccountNumber(), BigInteger.valueOf(amount));
        BatchDepositRequest batchDepositRequest = new BatchDepositRequest((long)accountDto.getCurrency().getValue(), Collections.singletonList(depositRequest), accountDto.getCurrency());
        TransferResult depositResult = ledgerStorageService.deposit(batchDepositRequest).get(0);
        if (depositResult.response() != 0) {
            log.error("Deposit failed for {}-{} due to: {}", accountHolderName, accountNumber, depositResult.description());
            throw new RuntimeException("Deposit failed");
        }

        return String.format("Money %s deposited to account name %s or number %s", amount, accountHolderName, accountNumber);
    }


    @ShellMethod(key = "withDrawFromAccount", value = "Withdraw money from an account")
    public String withDrawFromAccount(@ShellOption(value = "--amount") Long amount,
                                      @ShellOption(value = "--accountName", defaultValue = "") String accountHolderName,
                                      @ShellOption(value = "--accountNumber", defaultValue = "" ) String accountNumber) {

        Optional<AccountDetails> accountDetails =
                accountService.checkIfAccountExists(new AccountOperationDto(amount, accountHolderName, convertAccountNumber.apply(accountNumber)));
        if (accountDetails.isEmpty()) {
            log.error("Account name {} and/or number {} does not exist. Withdrawal aborted ", accountHolderName, accountNumber);
            return "";
        };

        AccountDto accountDto;
        if (accountDetails.get().accountType().equals(AccountType.GROUP)){
            accountDto =
                    accountService.findGroup(accountHolderName, convertAccountNumber.apply(accountNumber))
                            .orElseThrow(() -> new RuntimeException("Group not found"));
        } else {
            accountDto =
                    accountService.findIndividual(accountHolderName, convertAccountNumber.apply(accountNumber))
                            .orElseThrow(() -> new RuntimeException("Individual not found"));
        }

        WithdrawRequest withdrawRequest = new WithdrawRequest(accountDto.getAccountNumber(), BigInteger.valueOf(amount));
        BatchWithdrawRequest batchWithdrawRequest = new BatchWithdrawRequest( accountDto.getCurrency().getValue(), Collections.singletonList(withdrawRequest), accountDto.getCurrency());
        TransferResult withdrawResult = ledgerStorageService.withdraw(batchWithdrawRequest).get(0);
        if (withdrawResult.response() != 0) {
            log.error("Withdraw failed for {}-{} due to: {}", accountHolderName, accountNumber, withdrawResult.description());
            throw new RuntimeException("Deposit failed");
        }

        return String.format("Money %s withdrawn from account name %s or number %s", amount, accountHolderName, accountNumber);
    }

    @ShellMethod(key = "transferFunds", value = "Transfer funds from one account to another, either fromAccountName or fromAccountNumber is mandatory" +
            " Same goes for to account ")
    public String transferFunds(@ShellOption(value = "--amount") Long amount,
                                @ShellOption(help = "From account name", defaultValue = "") String fromAccountName,
                                @ShellOption(help = "From account number", defaultValue = "" ) String fromAccountNumber,
                                @ShellOption(help = "To account name", defaultValue = "") String toAccountName,
                                @ShellOption(help = "To account number", defaultValue = "" ) String toAccountNumber) {
        if (ObjectUtils.isEmpty(fromAccountName) &&  ObjectUtils.isEmpty(fromAccountNumber) ) {
            log.error("Both From account name and number cannot be empty");
            return "";
        }

        if (ObjectUtils.isEmpty(toAccountName) && ObjectUtils.isEmpty(toAccountNumber) ) {
            log.error("Both To account name and number cannot be empty");
            return "";
        }
        Optional<AccountDetails> fromAccountDetails =
                accountService.checkIfAccountExists(new AccountOperationDto(amount, fromAccountName, convertAccountNumber.apply(fromAccountNumber)));
        if (fromAccountDetails.isEmpty()) {
            log.error("Account name {} and/or number {} does not exist. Transfer aborted ", fromAccountName, fromAccountNumber);
            return "";
        };
        Optional<AccountDetails> toAccountDetails =
                accountService.checkIfAccountExists(new AccountOperationDto(amount, toAccountName, convertAccountNumber.apply(toAccountNumber)));
        if (toAccountDetails.isEmpty()) {
            log.error("Account name {} and/or number {} does not exist. Transfer aborted ", toAccountName, toAccountNumber);
            return "";
        };

        final AtomicReference<AccountDto> toIndividualDtoRef = new AtomicReference<>();
        final AtomicReference<AccountDto> fromIndividualDtoRef = new AtomicReference<>();

        if ( fromAccountDetails.get().accountType().equals(AccountType.GROUP) && toAccountDetails.get().accountType().equals(AccountType.GROUP)) {
            log.error("Cannot transfer between groups");
            return "";
        } else if ( fromAccountDetails.get().accountType().equals(AccountType.GROUP)) {
            Optional<IndividualDto> toIndividualDto = accountService.findIndividual(toAccountName, convertAccountNumber.apply(toAccountNumber));
            if (toIndividualDto.isEmpty()) {
                log.error("To account name {} and/or number {} does not exist. Transfer aborted ", toAccountName, toAccountNumber);
                return "";
            }
            if ( toIndividualDto.get().getGroups().stream().noneMatch(grp -> grp.getGroupName().equals(fromAccountName) ||
                    grp.getAccountNumber().equals(convertAccountNumber.apply(toAccountNumber)))) {
                log.error("To account name {} or number {} does not belong to group account name {} or number {}, transfer aborted",
                        toAccountName, toAccountNumber, fromAccountName, fromAccountNumber);
                return "";
            }
            fromIndividualDtoRef.set(accountService.findGroup(fromAccountName, convertAccountNumber.apply(fromAccountNumber))
                    .orElseThrow(() -> new RuntimeException("Group could not be found, something is buggy in code")));
            toIndividualDtoRef.set(toIndividualDto.orElseThrow(() -> new RuntimeException("Individual could not be found, something is wrong in code")));

        }else if ( toAccountDetails.get().accountType().equals(AccountType.GROUP)) {
            Optional<IndividualDto> fromIndividualDto = accountService.findIndividual(fromAccountName, convertAccountNumber.apply(fromAccountNumber));
            if (fromIndividualDto.isEmpty()) {
                log.error("From account name {} and/or number {} does not exist. Transfer aborted ", fromAccountName, fromAccountNumber);
                return "";
            }
            if ( fromIndividualDto.get().getGroups().stream().noneMatch(grp -> grp.getGroupName().equals(toAccountName) ||
                    grp.getAccountNumber().equals(convertAccountNumber.apply(toAccountNumber)))) {
                log.error("From Account name {} or number {} does not belong to group account name {} or number {}, transfer aborted",
                        fromAccountName, fromAccountNumber, toAccountName, toAccountNumber);
                return "";
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

        if ( !fromIndividualDtoRef.get().getCurrency().equals(toIndividualDtoRef.get().getCurrency()) ){

            BigInteger targetAmount = BigInteger.valueOf(amount * toIndividualDtoRef.get().getCurrency().getRate());
            List<TransferRequest> transferRequests = List.of(
            new TransferRequest(
                    fromIndividualDtoRef.get().getAccountNumber(), fromIndividualDtoRef.get().getCurrency().getValue(), BigInteger.valueOf(amount), fromIndividualDtoRef.get().getCurrency(), TransferFlags.LINKED),
            new TransferRequest(
                   toIndividualDtoRef.get().getCurrency().getValue(), toIndividualDtoRef.get().getAccountNumber(), targetAmount, toIndividualDtoRef.get().getCurrency(), null)
                );

            TransferResult transfersResult = ledgerStorageService.createTransfers(transferRequests).get(0);
            if (transfersResult.response() != 0) {
                log.error("Transfer failed from {}-{} to  {}-{}, caused by: {}", fromAccountName, fromAccountNumber, toAccountName, toAccountNumber, transfersResult.description());
                throw new RuntimeException("Transfer failed");
            }

            return String.format("Source money %s to target money %s is transferred from account name %s or number %s, " +
                    "to account name %s or number %s", amount, targetAmount, fromAccountName, fromAccountNumber, toAccountName, toAccountNumber);
        }

        TransferRequest transferRequest = new TransferRequest(fromIndividualDtoRef.get().getAccountNumber(), toIndividualDtoRef.get().getAccountNumber(), BigInteger.valueOf(amount), fromIndividualDtoRef.getOpaque().getCurrency(), null);
        TransferResult transfersResult = ledgerStorageService.createTransfers(Collections.singletonList(transferRequest)).get(0);

        if (transfersResult.response() != 0) {
            log.error("Transfer failed from {}-{} to  {}-{}, caused by: {}", fromAccountName, fromAccountNumber, toAccountName, toAccountNumber, transfersResult.description());
            throw new RuntimeException("Transfer failed");
        }

        return String.format("Money %s is transferred from account name %s or number %s, " +
                "to account name %s or number %s", amount, fromAccountName, fromAccountNumber, toAccountName, toAccountNumber);
    }
}
