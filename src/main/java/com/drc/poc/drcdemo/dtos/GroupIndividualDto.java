package com.drc.poc.drcdemo.dtos;

import com.drc.poc.drcdemo.entities.Currency;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = false)
@Valid
@Accessors(chain = true)
public class GroupIndividualDto extends AccountDto{
    @NotNull
    private String groupName;
    @NotNull
    private String individualName;

    public GroupIndividualDto( String groupName, String individualName) {
        this.groupName = groupName;
        this.individualName = individualName;
    }
}
