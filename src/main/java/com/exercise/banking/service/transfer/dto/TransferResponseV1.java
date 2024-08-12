package com.exercise.banking.service.transfer.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Schema(description = "Transfer Response")
public final class TransferResponseV1 {
    
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
    
    @NotNull(message = "Transferred amount is required")
    @Schema(description = "Transferred amount", example = "100.00")
    private final BigDecimal amount;
    
    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "EUR|GBP", message = "Currency must be one of the following:  EUR, GBP")
    @Schema(description = "Currency code in ISO 4217 format", example = "GBP")
    private final String currency;
    
    @NotNull(message = "Transfer type is required")
    @Schema(description = "Transfer type", example = "IntraBankTransfer")
    private final String transferType;
    
    @NotNull(message = "The timestamp is required")
    @Schema(description = "Timestamp in ISO 8601 format", example = "2024-08-11T17:26:13.581630Z")
    private final String timestamp; 

    // Private constructor to force usage of the builder
    private TransferResponseV1(Builder builder) {
        this.requestId = builder.requestId;
        this.transactionId = builder.transactionId;
        this.status = builder.status;
        this.balance = builder.balance;
        this.amount = builder.amount;
        this.currency = builder.currency;
        this.transferType = builder.transferType;
        this.timestamp = builder.timestamp != null ? builder.timestamp.toString() : Instant.now().toString();
    }

    public static class Builder {
        private UUID requestId;
        private UUID transactionId;
        private String status;
        private BigDecimal balance;
        private BigDecimal amount;
        private String currency;
        private String transferType;
        private Instant timestamp;

        public Builder withRequestId(UUID requestId) {
            this.requestId = requestId;
            return this;
        }

        public Builder withTransactionId(UUID transactionId) {
            this.transactionId = transactionId;
            return this;
        }

        public Builder withStatus(String status) {
            this.status = status;
            return this;
        }

        public Builder withBalance(BigDecimal balance) {
            this.balance = balance;
            return this;
        }

        public Builder withAmount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public Builder withCurrency(String currency) {
            this.currency = currency;
            return this;
        }

        public Builder withTransferType(String transferType) {
            this.transferType = transferType;
            return this;
        }

        public Builder withTimestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public TransferResponseV1 build() {
            return new TransferResponseV1(this);
        }
    }
}
