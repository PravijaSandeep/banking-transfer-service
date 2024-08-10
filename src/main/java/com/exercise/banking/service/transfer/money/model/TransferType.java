package com.exercise.banking.service.transfer.money.model;

public enum TransferType {
    INTRA_BANK_TRANSFER("IntraBankTransfer"), 
    INTER_BANK_TRANSFER("InterBankTransfer");

    private final String value;

    TransferType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
