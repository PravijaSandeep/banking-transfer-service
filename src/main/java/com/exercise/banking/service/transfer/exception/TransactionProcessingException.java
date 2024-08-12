package com.exercise.banking.service.transfer.exception;

import java.util.UUID;

public class TransactionProcessingException extends BaseTransferException{

	private static final long serialVersionUID = -4255593136241282228L;

	public TransactionProcessingException(UUID requestId,String msg) {
        super(requestId,msg);
    }
}
