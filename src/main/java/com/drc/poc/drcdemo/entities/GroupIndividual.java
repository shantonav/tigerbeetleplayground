package com.drc.poc.drcdemo.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "GROUP_INDIVIDUAL")
@Data
@Accessors(chain = true)
public class GroupIndividual {
    @EmbeddedId
    private GroupIndividualId id;

    @MapsId("groupid")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "GROUPID", nullable = false)
    private com.drc.poc.drcdemo.entities.Group groupid;

    @MapsId("individualid")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "INDIVIDUALID", nullable = false)
    private com.drc.poc.drcdemo.entities.Individual individualid;


}