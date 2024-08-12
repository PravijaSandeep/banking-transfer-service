package com.exercise.banking.service.transfer.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "transactions")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"transactionId","requestId", "payerAccount", "payee","amount","currency","timestamp","type","status"})
public class Transaction {
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "transaction_id", updatable = false, nullable = false)
    private UUID transactionId;
    
    @Column(nullable = false)
    private  UUID requestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_account_id", referencedColumnName = "accNum")
    private Account payerAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payee_id", referencedColumnName = "id")
    private Payee payee;

    @Column(nullable = false, precision = 20, scale = 2)
    private BigDecimal amount;
    
    @Column(nullable = false)
    private String currency;

    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @Column(nullable = false)
    private String type;


    @PrePersist
    protected void onCreate() {
        if (this.timestamp == null) {
            this.timestamp = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = TransactionStatus.PENDING;
        }
    }
}