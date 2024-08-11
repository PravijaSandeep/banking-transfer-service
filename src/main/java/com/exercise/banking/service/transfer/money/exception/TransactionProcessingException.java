package com.exercise.banking.service.transfer.money.exception;

public class TransactionProcessingException extends RuntimeException{
	private static final long serialVersionUID = 1L;

	public TransactionProcessingException(String message) {
		super(message);
	}
}
