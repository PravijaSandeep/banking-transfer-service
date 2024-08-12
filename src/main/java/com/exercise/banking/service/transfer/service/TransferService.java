package com.exercise.banking.service.transfer.service;

import com.exercise.banking.service.transfer.dto.TransferRequest;
import com.exercise.banking.service.transfer.dto.TransferResponse;


public interface TransferService {
	/**
	 * Transfers the amount as per the request details
	 * @param transferRequest
	 * @return response object
	 */
	TransferResponse performTransfer(TransferRequest transferRequest);
}
