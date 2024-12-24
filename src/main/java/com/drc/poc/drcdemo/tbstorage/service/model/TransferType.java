package com.drc.poc.drcdemo.tbstorage.service.model;

public enum TransferType {
    START_PENDING(2), VOID_PENDING(8), POST_PENDING(4), POST_DIRECT(0);

    int value;

    TransferType(int value) {
        this.value = value;
    }

    public int getValue() {return this.value;}
}
