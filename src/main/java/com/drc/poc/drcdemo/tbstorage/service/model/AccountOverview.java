package com.drc.poc.drcdemo.tbstorage.service.model;

import java.util.List;

public record AccountOverview(LookupAccountResult accountDetails, List<TransferOverview> transfers) {
}
