package com.drc.poc.drcdemo.tbstorage.service.model;

import java.util.UUID;

public record TransferResult(UUID transferId,
                             Integer response,
                             String description) {
}
