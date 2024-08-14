package com.exercise.banking.service.transfer.service;

import java.util.Optional;
import java.util.UUID;

import com.exercise.banking.service.transfer.model.Transaction;

public interface TransactionService {
	/**
	 * Saves the transaction to db
	 * @param txn
	 * @return saved transaction
	 */
	Transaction saveTransaction(Transaction txn);
	
	/**
	 * Finds the transaction by requestId 
	 * @param requestId
	 * @return matching transaction
	 */
	Optional<Transaction> findByRequestId(UUID requestId);
	

}
