package com.exercise.banking.service.transfer.exception;

import java.util.UUID;

public class AccountNotFoundException extends BaseTransferException {

	private static final long serialVersionUID = -6533894237752047382L;

	public AccountNotFoundException(UUID requestId) {
        super(requestId,"Account not found");
    }
}
