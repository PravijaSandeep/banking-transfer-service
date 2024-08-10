package com.exercise.banking.service.transfer.money.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.exercise.banking.service.transfer.money.model.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction,Long>{

}
