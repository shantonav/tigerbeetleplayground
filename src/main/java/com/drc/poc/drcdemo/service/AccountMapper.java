package com.drc.poc.drcdemo.service;

import com.drc.poc.drcdemo.dtos.GroupDto;
import com.drc.poc.drcdemo.dtos.IndividualDto;
import com.drc.poc.drcdemo.entities.Group;
import com.drc.poc.drcdemo.entities.Individual;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    Individual fromAcountDto(IndividualDto individualDto);

    Group fromAcountDto(GroupDto groupDto);

}
