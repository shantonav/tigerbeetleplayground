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
    public IndividualDto createAnAccount(IndividualDto individualDto) {
        Long accountNumber = System.currentTimeMillis();
        individualAccountRepo.save(accountMapper.fromAcountDto(individualDto).setIndivAccountNumber(accountNumber));
        // TODO : then call TigerBeetle Service to add this account to ledger
        return (IndividualDto) individualDto.setAccountNumber(accountNumber);
    }

    @Override
    public GroupDto createAGroupAccount(GroupDto groupDto) {
        Long accountNumber = System.currentTimeMillis();
        groupAccountRepo.save(accountMapper.fromAcountDto(groupDto).setGroupAccountNumber(accountNumber));
        // TODO : then call TigerBeetle Service to add this account to ledger
        return (GroupDto) groupDto.setAccountNumber(accountNumber);
    }

    @Override
    public boolean addIndividualToAGroup(GroupIndividualDto groupIndividual) {
        return false;
    }

}
