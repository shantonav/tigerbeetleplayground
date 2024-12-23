package com.drc.poc.drcdemo.service;

import com.drc.poc.drcdemo.dtos.AccountOperationDto;
import com.drc.poc.drcdemo.dtos.GroupDto;
import com.drc.poc.drcdemo.dtos.GroupIndividualDto;
import com.drc.poc.drcdemo.dtos.IndividualDto;
import com.drc.poc.drcdemo.entities.Currency;
import com.drc.poc.drcdemo.tbstorage.service.LedgerStorageService;
import com.drc.poc.drcdemo.tbstorage.service.model.BatchDepositRequest;
import com.drc.poc.drcdemo.tbstorage.service.model.BatchWithdrawRequest;
import com.drc.poc.drcdemo.tbstorage.service.model.DepositRequest;
import com.drc.poc.drcdemo.tbstorage.service.model.TransferRequest;
import com.drc.poc.drcdemo.tbstorage.service.model.TransferResult;
import com.drc.poc.drcdemo.tbstorage.service.model.WithdrawRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.util.ObjectUtils;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;

import static com.drc.poc.drcdemo.tbstorage.config.DemoBankAccounts.DEFAULT_BANK_ACCOUNT_ID;

@ShellComponent
@Slf4j
@AllArgsConstructor
public class ApplicationCommandReceptors {
    private final AccountServiceInterface accountService;
    private final LedgerStorageService ledgerStorageService;


    @ShellMethod(key = "createAccount", value = "Create an individual account")
    public String createAccount(@ShellOption(value = "--accountHolderName") String accountHolderName, @ShellOption(value = "--currency") Currency currency) {

        IndividualDto individualDto = accountService.createAnAccount(new IndividualDto(Currency.EUR, accountHolderName));

        return "Created account number " + individualDto.getAccountNumber() + " for " + accountHolderName + " for Currency " + currency;
    }

    @ShellMethod(key = "createAGroupAccount", value = "Create a Group account")
    public String createAGroupAccount(@ShellOption(value = "--groupName") String groupName, @ShellOption(value = "--currency") Currency currency) {

        GroupDto individualDto = accountService.createAGroupAccount(new GroupDto(Currency.EUR, groupName));

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

        DepositRequest depositRequest = new DepositRequest(Long.parseLong(accountNumber), BigInteger.valueOf(amount));
        BatchDepositRequest batchDepositRequest = new BatchDepositRequest(DEFAULT_BANK_ACCOUNT_ID, Collections.singletonList(depositRequest));
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

        WithdrawRequest withdrawRequest = new WithdrawRequest(Long.parseLong(accountNumber), BigInteger.valueOf(amount));
        BatchWithdrawRequest batchWithdrawRequest = new BatchWithdrawRequest(DEFAULT_BANK_ACCOUNT_ID, Collections.singletonList(withdrawRequest));
        TransferResult withdrawResult = ledgerStorageService.withdraw(batchWithdrawRequest).get(0);
        if (withdrawResult.response() != 0) {
            log.error("Deposit failed for {}-{} due to: {}", accountHolderName, accountNumber, withdrawResult.description());
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

        TransferRequest transferRequest = new TransferRequest(Long.parseLong(fromAccountNumber), Long.parseLong(toAccountNumber), BigInteger.valueOf(amount));
        TransferResult transfersResult = ledgerStorageService.createTransfers(Collections.singletonList(transferRequest)).get(0);

        if (transfersResult.response() != 0) {
            log.error("Transfer failed from {}-{} to  {}-{}, caused by: {}", fromAccountName, fromAccountNumber, toAccountName, toAccountNumber, transfersResult.description());
            throw new RuntimeException("Deposit failed");
        }

        return String.format("Money %s is transferred from account name %s or number %s, " +
                "to account name %s or number %s", amount, fromAccountName, fromAccountNumber, toAccountName, toAccountNumber);
    }
}
