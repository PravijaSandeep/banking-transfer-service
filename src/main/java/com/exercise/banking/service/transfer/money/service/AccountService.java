package com.exercise.banking.service.transfer.money.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.exercise.banking.service.transfer.money.exception.AccountNotFoundException;
import com.exercise.banking.service.transfer.money.exception.PayeeNotRegisteredException;
import com.exercise.banking.service.transfer.money.model.Account;
import com.exercise.banking.service.transfer.money.model.Payee;
import com.exercise.banking.service.transfer.money.repository.AccountRepository;
import com.exercise.banking.service.transfer.money.repository.PayeeRepository;
@Service
public class AccountService {

	private final AccountRepository accRepo;
	private final PayeeRepository payeeRepo;

	private static final Logger logger = LoggerFactory.getLogger(AccountService.class);


	public AccountService(AccountRepository accRepo, PayeeRepository payeeRepo) {
		this.accRepo = accRepo;
		this.payeeRepo = payeeRepo;
	}

	public Account getAccountByNumber(String accountNumber) {
		return accRepo.findById(accountNumber)
				.orElseThrow(() -> new AccountNotFoundException("Account not found"));
	}

	/**
	 * Search for Payee registered by the Payer.
	 * @param payerAccountNum
	 * @param payeeAccountNum
	 * @return Optional<Payee> 
	 */
	public Payee getPayeeByAccountNumbersOrThrow(String payerAccountNum, String payeeAccountNum, String payeeBankCode) {
	    return payeeRepo.findByPayerAccount_AccNumAndAccNum(payerAccountNum, payeeAccountNum)
	        .filter(payee -> payee.getBank().getCode().equals(payeeBankCode))  // Check the bank code
	        .orElseThrow(() -> {
	            logger.error("Payee not found or bank code does not match for accounts: {} and {} with bank code {}", 
	                         payerAccountNum, payeeAccountNum, payeeBankCode);
	            throw new PayeeNotRegisteredException(payeeAccountNum);
	        });
	}


}
