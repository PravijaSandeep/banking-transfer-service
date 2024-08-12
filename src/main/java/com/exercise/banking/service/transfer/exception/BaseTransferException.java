package com.exercise.banking.service.transfer.exception;

import java.util.UUID;
/**
 * Base exception class for Transfer service
 */
public class BaseTransferException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final UUID requestId;
    
    public BaseTransferException(UUID requestId, String msg) {
        super(msg);
        this.requestId = requestId;
    }

    public UUID getRequestId() {
        return requestId;
    }

    @Override
    public String getMessage() {
        return String.format("Request ID: %s - %s", requestId, super.getMessage());
    }
}

