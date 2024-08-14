package com.exercise.banking.service.transfer.service.impl;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.exercise.banking.service.transfer.model.Transaction;
import com.exercise.banking.service.transfer.repository.TransactionRepository;
import com.exercise.banking.service.transfer.service.TransactionService;
@Service
public class TransactionServiceImpl implements TransactionService{
	
    protected final TransactionRepository txnRepository;
    
    public TransactionServiceImpl(TransactionRepository txnRepository) {
    	this.txnRepository = txnRepository;
    }

	@Override
	public Transaction saveTransaction(Transaction txn) {
		return this.txnRepository.save(txn);
	}

	@Override
	public Optional<Transaction> findByRequestId(UUID requestId) {
		return this.txnRepository.findByRequestId(requestId);
	}

}
