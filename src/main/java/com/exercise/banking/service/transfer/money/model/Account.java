package com.exercise.banking.service.transfer.money.model;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "account")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"accNum", "holderName", "bank"})
public class Account {

    @Id
    private String accNum;   // The unique account number
    
    @Column(nullable = false)
    private BigDecimal balance;   // Account balance

    @Column(nullable = false)
    private String holderName;   // Name of the account holder

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_code", referencedColumnName = "code")
    private Bank bank;   // The bank this account is associated with

    @OneToMany(mappedBy = "payerAccount", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Payee> payees = new HashSet<>();   // Set of payees associated with this account
    
    public void addPayee(Payee payee) {
        payee.setPayerAccount(this); 
        this.payees.add(payee);      // Add the Payee to the set
    }
    
}
