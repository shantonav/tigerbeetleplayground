package com.drc.poc.drcdemo.repository;

import com.drc.poc.drcdemo.dtos.GroupIndividualDto;
import com.drc.poc.drcdemo.entities.GroupIndividual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupIndividualRepo extends JpaRepository<GroupIndividual, Long> {
}
