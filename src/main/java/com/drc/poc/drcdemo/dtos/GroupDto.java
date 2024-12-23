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
public class GroupDto extends AccountDto {
    @NotNull
    private  String groupName;

    public GroupDto(Currency currency, String groupName) {
        super(currency);
        this.groupName = groupName;
    }
}
