package com.drc.poc.drcdemo.dtos;

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
}
