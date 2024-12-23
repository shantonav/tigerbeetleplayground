package com.drc.poc.drcdemo.service;

import com.drc.poc.drcdemo.dtos.GroupDto;
import com.drc.poc.drcdemo.dtos.GroupIndividualDto;
import com.drc.poc.drcdemo.dtos.IndividualDto;

public interface AccountServiceInterface {
    public IndividualDto createAnAccount(IndividualDto individual);

    public GroupDto createAGroupAccount(GroupDto group);

    public boolean addIndividualToAGroup(GroupIndividualDto groupIndividual);
}
