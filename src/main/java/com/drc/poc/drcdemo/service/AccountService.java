package com.drc.poc.drcdemo.service;


import com.drc.poc.drcdemo.dtos.*;
import com.drc.poc.drcdemo.entities.Group;
import com.drc.poc.drcdemo.entities.GroupIndividual;
import com.drc.poc.drcdemo.entities.Individual;
import com.drc.poc.drcdemo.repository.GroupAccountRepo;
import com.drc.poc.drcdemo.repository.GroupIndividualRepo;
import com.drc.poc.drcdemo.repository.IndividualAccountRepo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import java.util.function.Function;

@Component
@AllArgsConstructor
@Slf4j
public class AccountService implements AccountServiceInterface{
    private final IndividualAccountRepo individualAccountRepo;
    private final AccountMapper accountMapper;
    private final GroupAccountRepo groupAccountRepo;
    private final GroupIndividualRepo groupIndividualRepo;
    private final Map<String, AccountDetails> map = new HashMap<>();
    private final BiFunction<Tupple, AccountType, AccountDetails> store =
            (tupple, accountType) -> map.put(tupple.accountName(), new AccountDetails(tupple.accountNumber(), accountType)) ;
    private final Function<Tupple, Boolean> check = tupple -> map.containsKey(tupple.accountName());

    @Override
    public IndividualDto createAnAccount(IndividualDto individualDto) {

        final Long accountNumber = System.currentTimeMillis();
        if ( check.apply(new Tupple(individualDto.getIndividualName(), accountNumber)) ){
            return accountMapper.fromIndividualDto(individualDto, map.get(individualDto.getIndividualName()).accountNumber());
        }
        store.apply(new Tupple(individualDto.getIndividualName(), accountNumber), AccountType.INDIVIDUAL);
        final Individual indiv;
        try {
            indiv = CompletableFuture.supplyAsync(() -> accountMapper.fromAcountDto(individualDto, accountNumber))
                    .thenApply(individualAccountRepo::save)
                    .thenApply(ind -> ind.setIndivAccountNumber(accountNumber))
                    .get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("ERROR! while creating an account: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }

        // TODO : then call TigerBeetle Service to add this account to ledger
        return (IndividualDto) individualDto.setAccountNumber(indiv.getIndivAccountNumber());
    }

    @Override
    public GroupDto createAGroupAccount(GroupDto groupDto)  {
        Long accountNumber = System.currentTimeMillis();
        if ( check.apply(new Tupple(groupDto.getGroupName(), accountNumber)) ){
            return accountMapper.fromGroupDto(groupDto, map.get(groupDto.getGroupName()).accountNumber());
        }
        store.apply(new Tupple(groupDto.getGroupName(), accountNumber), AccountType.GROUP);
        final Group group;
        try {
            group = CompletableFuture.supplyAsync(() -> accountMapper.fromAcountDto(groupDto, accountNumber))
                    .thenApply(groupAccountRepo::save)
                    .thenApply(grp -> grp.setGroupAccountNumber(accountNumber))
                    .get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("ERROR! while creating a group: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
        // TODO : then call TigerBeetle Service to add this account to ledger
        return (GroupDto) groupDto.setAccountNumber(group.getGroupAccountNumber());
    }

    @Override
    public boolean addIndividualToAGroup(GroupIndividualDto groupIndividualDto) {
        Individual individual =
                individualAccountRepo.findByIndividualName(groupIndividualDto.getIndividualName()).orElse(null);
        Group group = groupAccountRepo.findByGroupName(groupIndividualDto.getGroupName()).orElse(null);

        if (individual == null || group == null) {
            return false;
        }
        GroupIndividual grpInd;
        try {
            grpInd =
                    CompletableFuture.supplyAsync(() -> accountMapper.fromGroupIndividual(group, individual))
                    .thenApply(groupIndividualRepo::save)
                    .get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("ERROR! while adding account to group: {}", e.getMessage());
            throw new RuntimeException(e);
        }
        return grpInd.getId() != null;
    }

    @Override
    public Optional<AccountDetails> checkIfAccountExists(AccountOperationDto accountOperationDto) {
        Optional<AccountDetails> accountDetails
                = !ObjectUtils.isEmpty(accountOperationDto.accountName()) && map.containsKey(accountOperationDto.accountName()) ?
                Optional.of(map.get(accountOperationDto.accountName()))
                : Optional.empty();

        if (accountDetails.isEmpty() && !ObjectUtils.isEmpty(accountOperationDto.accountNumber()) && accountOperationDto.accountNumber() > 0 ) {
            accountDetails =  map.entrySet().stream()
                    .filter(entry -> Objects.nonNull(entry.getValue()) &&
                            entry.getValue().accountNumber().equals(accountOperationDto.accountNumber()))
                            .map(entry -> Optional.of(entry.getValue())).reduce( Optional.empty(),
                            (a, val) -> val);

        }

       return accountDetails;
    }

    @Override
    public TransferResult transferFunds(TransferDto transferDto) {
        return null;
    }

}
