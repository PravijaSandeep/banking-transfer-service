package com.exercise.banking.service.transfer.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Schema(description = "Transfer Request")
public class TransferRequest { // Remove 'final' modifier

    @NotNull(message = "Request ID is required")
    @Schema(description = "Request ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private final UUID requestId;

    @Size(min = 5, max = 8, message = "Payer account number must be between 5 and 8 digits")
    @Pattern(regexp = "\\d+", message = "Payer account number must contain only digits")
    @Schema(description = "Payer account number", example = "123456")
    private final String payerAccNumber;

    @Size(min = 5, max = 8, message = "Payee account number must be between 5 and 8 digits")
    @Pattern(regexp = "\\d+", message = "Payee account number must contain only digits")
    @Schema(description = "Payee account number", example = "978654")
    private final String payeeAccNumber;

    @Schema(description = "Payee bank name", example = "BANK_A")
    private final String payeeBankName;

    @NotBlank(message = "Payee bank code is required")
    @Size(min = 6, max = 6, message = "Payee bank code must be 6 characters")
    @Schema(description = "Payee bank code", example = "A00001")
    private final String payeeBankCode;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Transfer amount must be greater than 0")
    @Schema(description = "Amount to be transferred", example = "100.00")
    private final BigDecimal amount;
    
    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "EUR|GBP", message = "Currency must be one of the following:  EUR, GBP")
    @Schema(description = "Currency code in ISO 4217 format", example = "GBP")
    private final String currency;
    
    @Schema(description = "Timestamp in ISO 8601 format", example = "2024-08-11T17:26:13.581630Z")
    private final String timestamp;

    @JsonCreator
    public TransferRequest(
        @JsonProperty("requestId") UUID requestId,
        @JsonProperty("payerAccNumber") String payerAccNumber,
        @JsonProperty("payeeAccNumber") String payeeAccNumber,
        @JsonProperty("payeeBankName") String payeeBankName,
        @JsonProperty("payeeBankCode") String payeeBankCode,
        @JsonProperty("amount") BigDecimal amount,
        @JsonProperty("currency") String currency,
        @JsonProperty("timestamp") String timestamp
    ) {
        this.requestId = requestId;
        this.payerAccNumber = payerAccNumber;
        this.payeeAccNumber = payeeAccNumber;
        this.payeeBankName = payeeBankName;
        this.payeeBankCode = payeeBankCode;
        this.amount = amount;
        this.currency = currency;
        this.timestamp = timestamp != null ? timestamp : Instant.now().toString();
    }

   
}