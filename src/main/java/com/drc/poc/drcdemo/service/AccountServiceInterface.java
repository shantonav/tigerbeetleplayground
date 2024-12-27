package com.drc.poc.drcdemo.service;

import com.drc.poc.drcdemo.dtos.*;

import java.util.List;
import java.util.Optional;

public interface AccountServiceInterface {
    IndividualDto createAnAccount(IndividualDto individual);

    GroupDto createAGroupAccount(GroupDto group);

    boolean addIndividualToAGroup(GroupIndividualDto groupIndividual);

    Optional<AccountDetails> checkIfAccountExists(AccountOperationDto accountOperationDto);

    TransferResult transferFunds(TransferDto transferDto);

    List<AccountBalance> getBalanceForAllAccounts();
}
