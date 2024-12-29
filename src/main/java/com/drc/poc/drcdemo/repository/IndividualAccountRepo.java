package com.drc.poc.drcdemo.repository;

import com.drc.poc.drcdemo.entities.Individual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IndividualAccountRepo extends JpaRepository<Individual, Integer> {
    
    Optional<Individual> findByIndividualName(String individualName);

    Optional<Individual> findByIndivAccountNumberOrIndividualName(Long indivAccountNumber, String individualName);

    Optional<Individual> findByIndivAccountNumber(Integer indivAccountNumber);
}
