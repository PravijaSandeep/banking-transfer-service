package com.exercise.banking.service.transfer.service;

import java.util.UUID;

import com.exercise.banking.service.transfer.model.Account;
import com.exercise.banking.service.transfer.model.Payee;

public interface AccountService {
	
	/**
	 * Find and returns the account matched to the accountNumber,
	 * if account number is not found, it throws AccountNotFoundException
	 * @param accountNumber
	 * @param requestID
	 * @return matched account
	 */
	 Account getAccountByNumberOrThrow(String accountNumber, UUID requestID);
	 
	 /**
	  * Find the payee account registered by the payer account,
	  * if payee account is not found, it throws PayeeNotRegisteredException
	  * @param payerAccountNum
	  * @param payeeAccountNum
	  * @param payeeBankCode
	  * @param requestId
	  * @return
	  */
	 Payee getPayeeByAccountNumbersOrThrow(String payerAccountNum, String payeeAccountNum, String payeeBankCode, UUID requestId);

}
