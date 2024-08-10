package com.exercise.banking.service.transfer.money.exception;

public class PayeeNotRegisteredException extends RuntimeException{
	
	private static final long serialVersionUID = 1L;

	public PayeeNotRegisteredException(String payeeAccountNum, String payerAccountNum) {
        super(String.format("Transfer failed: Payee with account number '%s' is not registered by Payer '%s'.", payeeAccountNum,payerAccountNum));
	}
}
