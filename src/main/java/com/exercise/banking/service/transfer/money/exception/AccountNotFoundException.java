package com.exercise.banking.service.transfer.money.exception;

public class AccountNotFoundException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public AccountNotFoundException(String accountNum) {
        super("Account not found with ID: " + accountNum);
    }
}
