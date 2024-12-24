package com.drc.poc.drcdemo.tbstorage.service.model;

import java.util.List;

public record BatchDepositRequest(Long bankAccountId, List<DepositRequest> depositRequests) {
}
