package com.drc.poc.drcdemo.service;

import com.drc.poc.drcdemo.dtos.*;

import java.util.Optional;

public interface AccountServiceInterface {
    public IndividualDto createAnAccount(IndividualDto individual);

    public GroupDto createAGroupAccount(GroupDto group);

    public boolean addIndividualToAGroup(GroupIndividualDto groupIndividual);

    public Optional<AccountDetails> checkIfAccountExists(AccountOperationDto accountOperationDto);

    public TransferResult transferFunds(TransferDto transferDto);
}
