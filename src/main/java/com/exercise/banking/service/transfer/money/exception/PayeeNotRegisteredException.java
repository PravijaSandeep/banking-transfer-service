package com.exercise.banking.service.transfer.money.exception;

import java.util.UUID;

public class PayeeNotRegisteredException extends BaseTransferException{
	

	private static final long serialVersionUID = 2064413559066629999L;

	public PayeeNotRegisteredException(UUID requestId) {
        super(requestId, "Payee not registered");
	}
}
