package com.drc.poc.drcdemo.dtos;

import com.drc.poc.drcdemo.entities.Currency;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public abstract  class AccountDto {
    protected Long accountNumber;
    protected Long balance;
    protected Currency currency;

    public AccountDto(Currency currency) {
        this.currency = currency;
    }
}
