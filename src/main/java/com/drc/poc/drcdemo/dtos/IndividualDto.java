package com.drc.poc.drcdemo.dtos;

import com.drc.poc.drcdemo.entities.Currency;
import lombok.*;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = false)
@Valid
@Accessors(chain = true)
public class IndividualDto extends AccountDto{
    @NotNull
    private String individualName;

    public IndividualDto(Currency currency, String individualName) {
        super(currency);
        this.individualName = individualName;
    }
}
