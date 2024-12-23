package com.drc.poc.drcdemo.service;


import com.drc.poc.drcdemo.dtos.GroupDto;
import com.drc.poc.drcdemo.dtos.GroupIndividualDto;
import com.drc.poc.drcdemo.dtos.IndividualDto;
import com.drc.poc.drcdemo.repository.GroupAccountRepo;
import com.drc.poc.drcdemo.repository.IndividualAccountRepo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class AccountService implements AccountServiceInterface{
    private final IndividualAccountRepo individualAccountRepo;
    private final AccountMapper accountMapper;
    private final GroupAccountRepo groupAccountRepo;

    @Override
    public IndividualDto createAnAccount(IndividualDto individual) {
        Long accountNumber = System.currentTimeMillis();
        individualAccountRepo.save(accountMapper.fromAcountDto(individual).setIndivAccountNumber(accountNumber));
        // TODO : then call TigerBeetle Service to add this account to ledger
        return (IndividualDto) individual.setAccountNumber(accountNumber);
    }

    @Override
    public GroupDto createAGroupAccount(GroupDto group) {
        return null;
    }

    @Override
    public boolean addIndividualToAGroup(GroupIndividualDto groupIndividual) {
        return false;
    }

}
