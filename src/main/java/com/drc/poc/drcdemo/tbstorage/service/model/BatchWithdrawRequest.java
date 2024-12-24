package com.drc.poc.drcdemo.tbstorage.service.model;

import java.util.List;

public record BatchWithdrawRequest(Long accountWithdrawId, List<WithdrawRequest> withdrawRequests) {
}
