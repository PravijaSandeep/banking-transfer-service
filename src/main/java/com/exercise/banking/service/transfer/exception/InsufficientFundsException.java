package com.exercise.banking.service.transfer.exception;

import java.util.UUID;

public class InsufficientFundsException extends BaseTransferException{
	
	private static final long serialVersionUID = 1L;

	public InsufficientFundsException(UUID requestId) {
        super(requestId,"Insuffient Fund in Payer account");
    }
}