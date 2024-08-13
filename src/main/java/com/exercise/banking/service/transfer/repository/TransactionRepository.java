package com.exercise.banking.service.transfer.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.exercise.banking.service.transfer.model.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction,UUID>{
	Optional<Transaction> findByRequestId(UUID requestId);
}
