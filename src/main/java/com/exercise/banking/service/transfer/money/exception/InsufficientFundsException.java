package com.exercise.banking.service.transfer.money.exception;

public class InsufficientFundsException extends RuntimeException{
	
	private static final long serialVersionUID = 1L;

	public InsufficientFundsException(String accountNum) {

		super(String.format("Insufficient funds in account %s", accountNum));
	}
}