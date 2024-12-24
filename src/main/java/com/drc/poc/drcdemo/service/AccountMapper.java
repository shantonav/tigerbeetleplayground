package com.drc.poc.drcdemo.service;

import com.drc.poc.drcdemo.dtos.GroupDto;
import com.drc.poc.drcdemo.dtos.IndividualDto;
import com.drc.poc.drcdemo.entities.Group;
import com.drc.poc.drcdemo.entities.GroupIndividual;
import com.drc.poc.drcdemo.entities.Individual;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    @Mapping(target = "indivAccountNumber", source = "accountNumber")
    Individual fromAcountDto(IndividualDto individualDto, Long accountNumber);

    @Mapping(target = "accountNumber", source = "accountNumber")
    IndividualDto fromIndividualDto(IndividualDto individualDto, Long accountNumber);

    @Mapping(target = "groupAccountNumber", source = "accountNumber")
    Group fromAcountDto(GroupDto groupDto, Long accountNumber);

    @Mapping(target = "accountNumber", source = "accountNumber")
    GroupDto fromGroupDto(GroupDto groupDto, Long accountNumber);


    @Mapping(target = "id.groupid" , source = "group.id")
    @Mapping(target = "id.individualid" , source = "individual.id")
    GroupIndividual fromGroupIndividual(Group group, Individual individual);

}
