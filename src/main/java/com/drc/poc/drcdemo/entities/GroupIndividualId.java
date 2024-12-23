package com.drc.poc.drcdemo.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.Hibernate;

import java.util.Objects;

@Embeddable
@Data
@EqualsAndHashCode(callSuper = false)
public class GroupIndividualId implements java.io.Serializable {
    private static final long serialVersionUID = -1229441778252365616L;
    @Column(name = "GROUPID", nullable = false)
    private Integer groupid;

    @Column(name = "INDIVIDUALID", nullable = false)
    private Integer individualid;


}