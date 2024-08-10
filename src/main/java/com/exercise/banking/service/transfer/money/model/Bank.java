package com.exercise.banking.service.transfer.money.model;

import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
public class Bank {

    @Id
    @Column(length = 10)
    private String code;   // The unique code for the bank

    @Column(nullable = false)
    private String name;   // The name of the bank, cannot be null

    @OneToMany(mappedBy = "bank", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Account> accounts;   // List of accounts associated with this bank
}
