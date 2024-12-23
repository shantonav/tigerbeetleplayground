package com.drc.poc.drcdemo.repository;

import com.drc.poc.drcdemo.entities.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupAccountRepo extends JpaRepository<Group, Integer> {

    Optional<Group> findByGroupName(String groupName);

    Optional<Group> findByGroupAccountNumber(Integer groupAccountNumber);
}
