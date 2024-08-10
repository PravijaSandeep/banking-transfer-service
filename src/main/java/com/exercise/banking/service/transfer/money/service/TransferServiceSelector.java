package com.exercise.banking.service.transfer.money.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.exercise.banking.service.transfer.money.service.impl.InterBankTransferService;
import com.exercise.banking.service.transfer.money.service.impl.IntraBankTransferService;

@Component
public class TransferServiceSelector {
	private static final Logger logger = LoggerFactory.getLogger(TransferServiceSelector.class);

	private final InterBankTransferService interBankSvc;
	
	private final IntraBankTransferService intraBankSvc;
	
	public TransferServiceSelector(InterBankTransferService interBankSvc,IntraBankTransferService intraBankSvc) {
		this.interBankSvc = interBankSvc;
		this.intraBankSvc = intraBankSvc;
	}
	
	/**
	 * Gets the matching transfer service for the input request.
	 * 
	 * @param payerBankCode
	 * @param payeeBankCode
	 * @return service for intra bank transfer and inter bank transfer
	 */
	public TransferService getService(String payerBankCode, String payeeBankCode)  {
		if (payeeBankCode == null) {
            throw new IllegalArgumentException("Payee bank code cannot be null");
        }
		
		logger.info("Payer Bankcode : {} Payee Bankcode:{}",payerBankCode,payeeBankCode);
		
		if(payerBankCode.equals(payeeBankCode)) {
			
			return intraBankSvc;
		}else {
			return interBankSvc;
		}
		
	}

}
