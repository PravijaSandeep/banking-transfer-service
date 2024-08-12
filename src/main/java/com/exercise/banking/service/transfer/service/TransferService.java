package com.exercise.banking.service.transfer.service;

import com.exercise.banking.service.transfer.dto.TransferRequestV1;
import com.exercise.banking.service.transfer.dto.TransferResponseV1;


public interface TransferService {
	/**
	 * Transfers the amount as per the request details
	 * @param transferRequest
	 * @return response object
	 */
	TransferResponseV1 performTransferV1(TransferRequestV1 transferRequest);
}
