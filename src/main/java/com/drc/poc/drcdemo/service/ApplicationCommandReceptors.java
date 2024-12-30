package com.drc.poc.drcdemo.service;

import com.drc.poc.drcdemo.dtos.*;
import com.drc.poc.drcdemo.entities.Currency;
import com.drc.poc.drcdemo.tbstorage.service.model.TransferResult;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@ShellComponent
@Slf4j
@AllArgsConstructor
public class ApplicationCommandReceptors {
    private final AccountServiceInterface accountService;
    private final TransferServiceInterface transferService;


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


    @ShellMethod(key = "depositToAccount", value = "Deposit money to an account")
    public String depositToAccount(@ShellOption(value = "--amount") Long amount,
                                   @ShellOption(value = "--accountName", defaultValue = "") String accountHolderName,
                                   @ShellOption(value = "--accountNumber", defaultValue = "" ) String accountNumber) {
        Optional<AccountDetails> accountDetails =
                accountService.checkIfAccountExists(new AccountOperationDto(amount, accountHolderName, Constants.convertAccountNumber.apply(accountNumber)));
        if (accountDetails.isEmpty()){
            log.error("Account name {} and/or number {} does not exist. Deposit aborted ", accountHolderName, accountNumber);
            return "";
        }
        AccountDto accountDto = getAccountDto(accountHolderName, accountNumber, accountDetails);

        TransferResult depositResult = transferService.deposit(amount, accountDto);

        if (depositResult.response() != 0) {
            log.error("Deposit failed for {}-{} due to: {}", accountHolderName, accountNumber, depositResult.description());
            throw new RuntimeException("Deposit failed");
        }
        return String.format("Money %s deposited to account name %s or number %s", amount, accountHolderName, accountNumber);
    }

    private AccountDto getAccountDto(String accountHolderName, String accountNumber, Optional<AccountDetails> accountDetails) {
        AccountDto accountDto;
        if (accountDetails.get().accountType().equals(AccountType.GROUP)){
            accountDto =
                    accountService.findGroup(accountHolderName, Constants.convertAccountNumber.apply(accountNumber))
                            .orElseThrow(() -> new RuntimeException("Group not found"));

        } else {
            accountDto =
                    accountService.findIndividual(accountHolderName, Constants.convertAccountNumber.apply(accountNumber))
                            .orElseThrow(() -> new RuntimeException("Individual not found"));
        }
        return accountDto;
    }


    @ShellMethod(key = "withDrawFromAccount", value = "Withdraw money from an account")
    public String withDrawFromAccount(@ShellOption(value = "--amount") Long amount,
                                      @ShellOption(value = "--accountName", defaultValue = "") String accountHolderName,
                                      @ShellOption(value = "--accountNumber", defaultValue = "" ) String accountNumber) {

        Optional<AccountDetails> accountDetails =
                accountService.checkIfAccountExists(new AccountOperationDto(amount, accountHolderName, Constants.convertAccountNumber.apply(accountNumber)));
        if (accountDetails.isEmpty()) {
            log.error("Account name {} and/or number {} does not exist. Withdrawal aborted ", accountHolderName, accountNumber);
            return "";
        }

        AccountDto accountDto = getAccountDto(accountHolderName, accountNumber, accountDetails);

        TransferResult withdrawResult = transferService.withDraw(amount, accountDto);
        if (withdrawResult.response() != 0) {
            log.error("Withdraw failed for {}-{} due to: {}", accountHolderName, accountNumber, withdrawResult.description());
            throw new RuntimeException("Deposit failed");
        }

        return String.format("Money %s withdrawn from account name %s or number %s", amount, accountHolderName, accountNumber);
    }


    private final BiFunction<String, String, Boolean> stringStringBooleanBiFunction =
            (fromAccountName1, fromAccountNumber1) -> ObjectUtils.isEmpty(fromAccountName1) && ObjectUtils.isEmpty(fromAccountNumber1);

    @ShellMethod(key = "transferFunds", value = "Transfer funds from one account to another, either fromAccountName or fromAccountNumber is mandatory" +
            " Same goes for to account ")
    public String transferFunds(@ShellOption(value = "--amount") Long amount,
                                @ShellOption(help = "From account name", defaultValue = "") String fromAccountName,
                                @ShellOption(help = "From account number", defaultValue = "" ) String fromAccountNumber,
                                @ShellOption(help = "To account name", defaultValue = "") String toAccountName,
                                @ShellOption(help = "To account number", defaultValue = "" ) String toAccountNumber) {

        if (stringStringBooleanBiFunction.apply(fromAccountName, fromAccountNumber)) {
            log.error("Both From account name and number cannot be empty");
            return "";
        }

        if (stringStringBooleanBiFunction.apply(toAccountName, toAccountNumber) ) {
            log.error("Both To account name and number cannot be empty");
            return "";
        }
        Optional<AccountDetails> fromAccountDetails =
                accountService.checkIfAccountExists(new AccountOperationDto(amount, fromAccountName, Constants.convertAccountNumber.apply(fromAccountNumber)));
        if (fromAccountDetails.isEmpty()) {
            log.error("Account name {} and/or number {} does not exist. Transfer aborted ", fromAccountName, fromAccountNumber);
            return "";
        };
        Optional<AccountDetails> toAccountDetails =
                accountService.checkIfAccountExists(new AccountOperationDto(amount, toAccountName, Constants.convertAccountNumber.apply(toAccountNumber)));
        if (toAccountDetails.isEmpty()) {
            log.error("Account name {} and/or number {} does not exist. Transfer aborted ", toAccountName, toAccountNumber);
            return "";
        };

       TransferResult transferResult = transferService.transferFunds(fromAccountDetails.get(), toAccountDetails.get(), amount, fromAccountName, fromAccountNumber, toAccountName, toAccountNumber);

        return transferResult.description();


    }



}
