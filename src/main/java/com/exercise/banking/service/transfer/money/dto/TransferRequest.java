package com.exercise.banking.service.transfer.money.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TransferRequest {

    @NotBlank(message = "Payer account number is required")
    private String payerAccNumber;

    @NotBlank(message = "Payee account number is required")
    private String payeeAccNumber;

    @NotBlank(message = "Payee bank name is required")
    private String payeeBankName;

    @NotBlank(message = "Payee bank code is required")
    private String payeeBankCode;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Transfer amount must be greater than 0")
    private BigDecimal amount;
}
