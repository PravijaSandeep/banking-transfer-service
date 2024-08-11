package com.exercise.banking.service.transfer.money.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@RequiredArgsConstructor
@ToString
public final class TransferRequest {

    @NotBlank(message = "Payer account number is required")
    private final String payerAccNumber;

    @NotBlank(message = "Payee account number is required")
    private final String payeeAccNumber;

    @NotBlank(message = "Payee bank name is required")
    private final String payeeBankName;

    @NotBlank(message = "Payee bank code is required")
    private final String payeeBankCode;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Transfer amount must be greater than 0")
    private final BigDecimal amount;
}
