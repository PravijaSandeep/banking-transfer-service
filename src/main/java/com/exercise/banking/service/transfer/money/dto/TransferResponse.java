package com.exercise.banking.service.transfer.money.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Schema(description = "Transfer Response")
public final class TransferResponse {
    
    @NotNull(message = "Request ID is required")
    @Schema(description = "Request ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private final UUID requestId;
    
    @NotNull(message = "Transaction ID is required")
    @Schema(description = "Transaction ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private final UUID transactionId;
    
    @NotNull(message = "Status of transaction is required")
    @Schema(description = "Transaction Status", example = "SUCCESS")
    private final String status;
    
    @NotNull(message = "Balance amount in Payer account is required")
    @Schema(description = "Payer account balance", example = "900.00")
    private final BigDecimal balance;
    
    @NotNull(message = "Transfered amount is required")
    @Schema(description = "Transfered amount", example = "100.00")
    private final BigDecimal amount;
    
    @NotNull(message = "Transfer type is required")
    @Schema(description = "Transfer type", example = "IntraBankTransfer")
    private final String transferType;
    
    @NotNull(message = "The timestamp is required")
    @Schema(description = "Timestamp in ISO 8601 format", example = "2024-08-11T17:26:13.581630Z")
    private final String timestamp; 
    
    public TransferResponse(
        UUID requestId,
        UUID transactionId,
        String status,
        BigDecimal balance,
        BigDecimal amount,
        String transferType,
        Instant timestamp
    ) {
        this.requestId = requestId;
        this.transactionId = transactionId;
        this.status = status;
        this.balance = balance;
        this.amount = amount;
        this.transferType = transferType;
        // Set the timestamp to now if it's not provided
        this.timestamp = timestamp != null ? timestamp.toString() : Instant.now().toString();

    }
}
