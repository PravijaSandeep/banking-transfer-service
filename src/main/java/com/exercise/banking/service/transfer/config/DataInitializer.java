package com.exercise.banking.service.transfer.config;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.exercise.banking.service.transfer.model.Account;
import com.exercise.banking.service.transfer.model.Bank;
import com.exercise.banking.service.transfer.model.Payee;
import com.exercise.banking.service.transfer.repository.AccountRepository;
import com.exercise.banking.service.transfer.repository.BankRepository;
import com.exercise.banking.service.transfer.repository.PayeeRepository;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner loadData(BankRepository bankRepository, AccountRepository accountRepository, PayeeRepository payeeRepository) {
        return args -> {
            // Create and save Banks
            Bank bankA = new Bank();
            bankA.setCode("A00001");
            bankA.setName("BANK_A");

            Bank bankB = new Bank();
            bankB.setCode("B00001");
            bankB.setName("BANK_B");

            bankRepository.save(bankA);
            bankRepository.save(bankB);

            // Create and save Accounts
            Account account1 = new Account("123456", new BigDecimal("1000.00"), "Person1", bankA, new HashSet<>());
            Account account2 = new Account("789123", new BigDecimal("2000.00"), "Person2", bankA, new HashSet<>());
            Account account3 = new Account("978654", new BigDecimal("3000.00"), "Person3", bankA, new HashSet<>());

            accountRepository.save(account1);
            accountRepository.save(account2);
            accountRepository.save(account3);

            // Create and save Payees for account1
            Payee payee1 = new Payee(null, "Person1-Payee1", "978654",  bankA, account1);
            Payee payee2 = new Payee(null, "Person1-Payee2", "654321",  bankB, account1);

            account1.setPayees(Set.of(payee1,payee2));
            accountRepository.save(account1);  // This will cascade the save to payees

            // Create and save a Payee for account3
            Payee payee3 = new Payee(null, "Person3-Payee1", "654321", bankB, account3);
            account3.setPayees(Set.of(payee3));
            accountRepository.save(account3);  // This will cascade the save to payees
        };
    }
}
