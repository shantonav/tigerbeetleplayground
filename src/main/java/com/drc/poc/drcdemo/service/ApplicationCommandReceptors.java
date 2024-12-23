package com.drc.poc.drcdemo.service;

import com.drc.poc.drcdemo.dtos.IndividualDto;
import com.drc.poc.drcdemo.entities.Currency;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
@Slf4j
@AllArgsConstructor
public class ApplicationCommandReceptors {
    private final AccountServiceInterface accountService;


    @ShellMethod(key = "createAccount", value = "Create an individual account")
    public String createAccount(@ShellOption(value = "--accountHolderName") String accountHolderName, @ShellOption(value = "--currency") Currency currency) {

        IndividualDto individualDto = accountService.createAnAccount(new IndividualDto(Currency.EUR, accountHolderName));

        return "Created account number " + individualDto.getAccountNumber() + " for " + accountHolderName + " for Currency " + currency;
    }

    @ShellMethod(key = "goodbye", value = "Say goodbye")
    public String goodbye() {
        return "Goodbye!";
    }
}
