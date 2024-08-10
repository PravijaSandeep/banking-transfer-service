package com.exercise.banking.service.transfer.money.exception;

public class TransactionProcessingException extends RuntimeException{
	private static final long serialVersionUID = 1L;

	public TransactionProcessingException(Long txnId, String message, Throwable cause) {
		super(String.format("Transaction ID %d: %s", txnId, message), cause);
	}
}
