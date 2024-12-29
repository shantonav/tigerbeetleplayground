package com.drc.poc.drcdemo.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "\"Group\"")
@Data
@Accessors(chain = true)
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "GROUPID", nullable = false)
    private Integer id;

    @Lob
    @Column(name = "GROUPNAME", nullable = false)
    private String groupName;

    @Column(name = "GROUPACCOUNTNUMBER", nullable = false)
    private Long groupAccountNumber;


    @Column(name = "BALANCE")
    private Long balance;

    @Column(name = "CURRENCY", nullable = false)
    private Currency currency;

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<com.drc.poc.drcdemo.entities.Individual> individuals = new LinkedHashSet<>();

}