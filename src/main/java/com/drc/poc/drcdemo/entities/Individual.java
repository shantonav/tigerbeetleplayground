package com.drc.poc.drcdemo.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "\"Individual\"")
@Data
@Accessors(chain = true)
public class Individual {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "INDIVIDUALID", nullable = false)
    private Integer id;

    @Lob
    @Column(name = "INDIVIDUALNAME", nullable = false)
    private String individualName;

    @Column(name = "INDIVACCOUNTNUMBER", nullable = false)
    private Long indivAccountNumber;

    @Column(name = "BALANCE")
    private Long balance;

    @Column(name = "CURRENCY", nullable = false)
    private Currency currency;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "GROUP_INDIVIDUAL",
            joinColumns = @JoinColumn(name = "INDIVIDUALID"),
            inverseJoinColumns = @JoinColumn(name = "groupid"))
    private Set<Group> groups = new LinkedHashSet<>();


}