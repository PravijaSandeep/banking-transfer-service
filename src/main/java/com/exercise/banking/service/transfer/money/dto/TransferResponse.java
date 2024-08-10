package com.exercise.banking.service.transfer.money.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class TransferResponse {
	private String transactionId;
    private String status;
    private BigDecimal balance;
    private BigDecimal amount;
    private String transferType;
}
