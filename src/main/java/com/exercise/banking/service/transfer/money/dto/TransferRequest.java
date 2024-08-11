package com.exercise.banking.service.transfer.money.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Schema(description = "Transfer Request")
public final class TransferRequest {
	
	@NotNull(message = "Request ID is required")
	@Schema(description = "Request ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private final UUID requestId;

    @NotBlank(message = "Payer account number is required")
    @Schema(description = "Payer account number", example = "ACC001")
    private final String payerAccNumber;

    @NotBlank(message = "Payee account number is required")
    @Schema(description = "Payee account number", example = "ACC003")
    private final String payeeAccNumber;

    @NotBlank(message = "Payee bank name is required")
    @Schema(description = "Payee bank name", example = "BANK_A")
    private final String payeeBankName;

    @NotBlank(message = "Payee bank code is required")
    @Schema(description = "Payee bank code", example = "A0001")
    private final String payeeBankCode;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Transfer amount must be greater than 0")
    @Schema(description = "Amount to be transferred", example = "100.00")
    private final BigDecimal amount;
    
    @Schema(description = "Timestamp", example = "2024-08-11T17:26:13.58163")
    private final LocalDateTime timestamp; 

    @JsonCreator
    public TransferRequest(
        @JsonProperty("requestId") UUID requestId,
        @JsonProperty("payerAccNumber") String payerAccNumber,
        @JsonProperty("payeeAccNumber") String payeeAccNumber,
        @JsonProperty("payeeBankName") String payeeBankName,
        @JsonProperty("payeeBankCode") String payeeBankCode,
        @JsonProperty("amount") BigDecimal amount,
        @JsonProperty("timestamp") LocalDateTime timestamp
    ) {
        this.requestId = requestId;
        this.payerAccNumber = payerAccNumber;
        this.payeeAccNumber = payeeAccNumber;
        this.payeeBankName = payeeBankName;
        this.payeeBankCode = payeeBankCode;
        this.amount = amount;
        this.timestamp = timestamp;
    }
}
