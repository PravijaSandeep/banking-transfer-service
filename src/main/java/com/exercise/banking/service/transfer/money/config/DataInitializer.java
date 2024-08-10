package com.exercise.banking.service.transfer.money.config;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.exercise.banking.service.transfer.money.model.Account;
import com.exercise.banking.service.transfer.money.model.Bank;
import com.exercise.banking.service.transfer.money.model.Payee;
import com.exercise.banking.service.transfer.money.model.PayeeType;
import com.exercise.banking.service.transfer.money.repository.AccountRepository;
import com.exercise.banking.service.transfer.money.repository.BankRepository;
import com.exercise.banking.service.transfer.money.repository.PayeeRepository;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner loadData(BankRepository bankRepository, AccountRepository accountRepository, PayeeRepository payeeRepository) {
        return args -> {
            // Create and save Banks
            Bank natwestBank = new Bank();
            natwestBank.setCode("nw123");
            natwestBank.setName("NatWest");

            Bank hsbcBank = new Bank();
            hsbcBank.setCode("hsbc123");
            hsbcBank.setName("HSBC");

            bankRepository.save(natwestBank);
            bankRepository.save(hsbcBank);

            // Create and save Accounts
            Account account1 = new Account("ACC001", new BigDecimal("1000.00"), "Person1", natwestBank, new HashSet<>());
            Account account2 = new Account("ACC002", new BigDecimal("2000.00"), "Person2", natwestBank, new HashSet<>());
            Account account3 = new Account("ACC003", new BigDecimal("3000.00"), "Person3", natwestBank, new HashSet<>());

            accountRepository.save(account1);
            accountRepository.save(account2);
            accountRepository.save(account3);

            // Create and save Payees for account1
            Payee payee1 = new Payee(null, "Person1-Payee1", "ACC003", PayeeType.INTRA_BANK, natwestBank, account1);
            Payee payee2 = new Payee(null, "Person1-Payee2", "OACC001", PayeeType.INTER_BANK, hsbcBank, account1);

            account1.setPayees(Set.of(payee1,payee2));
            accountRepository.save(account1);  // This will cascade the save to payees

            // Create and save a Payee for account3
            Payee payee3 = new Payee(null, "Person3-Payee1", "OACC001", PayeeType.INTER_BANK, hsbcBank, account3);
            account3.setPayees(Set.of(payee3));
            accountRepository.save(account3);  // This will cascade the save to payees
        };
    }
}
