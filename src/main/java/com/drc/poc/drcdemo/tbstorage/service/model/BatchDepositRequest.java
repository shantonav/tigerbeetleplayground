package com.drc.poc.drcdemo.tbstorage.service.model;

import com.drc.poc.drcdemo.entities.Currency;

import java.util.List;

public record BatchDepositRequest(Long bankAccountId, List<DepositRequest> depositRequests, Currency currency) {
}
