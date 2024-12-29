package com.drc.poc.drcdemo.dtos;

import com.drc.poc.drcdemo.entities.Currency;
import lombok.*;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = false)
@Valid
@Accessors(chain = true)
public class IndividualDto extends AccountDto{
    @NotNull
    private String individualName;
    private Set<GroupDto> groups;

    @Default
    public IndividualDto(Currency currency, String individualName) {
        super(currency);
        this.individualName = individualName;
    }

    public IndividualDto(Currency currency, Long accountNumber,
                         String individualName, Set<GroupDto> groups) {
        super(currency, accountNumber);
        this.individualName = individualName;
        this.groups = groups;
    }
}
