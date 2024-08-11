package com.exercise.banking.service.transfer.money.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@RequiredArgsConstructor
@ToString
public final class TransferResponse {
    // Transaction Id generated in GUID Format
    private final String transactionId;
    // Status of Transaction
    private final String status;
    // Balance in Payer account
    private final BigDecimal balance;
    // Transferred amount
    private final BigDecimal amount;
    // Indicates the type of transfer like intra bank or inter bank
    private final String transferType;
}
