package com.exercise.banking.service.transfer.money.model;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Account {

    @Id
    private String accNum;   // The unique account number

    private BigDecimal balance;   // Account balance

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
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return Objects.equals(accNum, account.accNum) &&
               Objects.equals(holderName, account.holderName) &&
               Objects.equals(bank, account.bank);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accNum, holderName, bank);
    }
}
