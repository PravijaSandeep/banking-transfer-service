package com.exercise.banking.service.transfer.money.model;

import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Payee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nickName;  // The nickname for the payee

    private String accNum;    // The payee's account number
    
    private PayeeType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_code", referencedColumnName = "code")
    private Bank bank;        // The bank this payee is associated with

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", referencedColumnName = "accNum")
    private Account payerAccount;  // The main account this payee is associated with
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Payee payee = (Payee) o;
        return Objects.equals(accNum, payee.accNum) &&
               Objects.equals(nickName, payee.nickName) &&
               Objects.equals(bank, payee.bank) &&
               Objects.equals(payerAccount, payee.payerAccount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accNum, nickName, bank, payerAccount);
    }
}
