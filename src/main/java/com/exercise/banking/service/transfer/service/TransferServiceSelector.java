package com.exercise.banking.service.transfer.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.exercise.banking.service.transfer.model.TransferType;

@Component
public class TransferServiceSelector {
	private static final Logger logger = LoggerFactory.getLogger(TransferServiceSelector.class);
	
	private final Map<TransferType, TransferService> services;
	
	public TransferServiceSelector(Map<TransferType, TransferService> services) {
		this.services = services;
	}

	/**
	 * Gets the matching transfer service for the input request.
	 * 
	 * @param payerBankCode
	 * @param payeeBankCode
	 * @return service for intra bank transfer and inter bank transfer
	 */
	public TransferService getService(String payerBankCode, String payeeBankCode)  {
		TransferType transferType = TransferType.INTER_BANK_TRANSFER;
		if(payerBankCode.equals(payeeBankCode)) {
			transferType = TransferType.INTRA_BANK_TRANSFER;
		}
	    logger.info("Identified transfer type for bayee bank code {} is {}", payeeBankCode, transferType);

		return services.get(transferType);
	}

}
