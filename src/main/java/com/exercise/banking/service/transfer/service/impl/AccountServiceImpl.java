package com.exercise.banking.service.transfer.service.impl;

import java.math.BigDecimal;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.exercise.banking.service.transfer.exception.AccountNotFoundException;
import com.exercise.banking.service.transfer.exception.InsufficientFundsException;
import com.exercise.banking.service.transfer.exception.PayeeNotRegisteredException;
import com.exercise.banking.service.transfer.model.Account;
import com.exercise.banking.service.transfer.model.Payee;
import com.exercise.banking.service.transfer.repository.AccountRepository;
import com.exercise.banking.service.transfer.repository.PayeeRepository;
import com.exercise.banking.service.transfer.service.AccountService;
@Service
public class AccountServiceImpl implements AccountService{

	private final AccountRepository accRepo;
	private final PayeeRepository payeeRepo;

	private static final Logger logger = LoggerFactory.getLogger(AccountServiceImpl.class);

	
	public AccountServiceImpl(AccountRepository accRepo, PayeeRepository payeeRepo) {
		this.accRepo = accRepo;
		this.payeeRepo = payeeRepo;
	}

	@Override
	public Account getAccountByNumberOrThrow(String accountNumber, UUID requestID) {
		return accRepo.findById(accountNumber)
				.orElseThrow(() -> new AccountNotFoundException(requestID));
	}

	/**
	 * Search for Payee registered by the Payer.
	 * @param payerAccountNum
	 * @param payeeAccountNum
	 * @param requestId 
	 * @return Optional<Payee> 
	 */
	@Override
	public Payee getPayeeByAccountNumbersOrThrow(String payerAccountNum, String payeeAccountNum, String payeeBankCode, UUID requestId) {
	    return payeeRepo.findByPayerAccount_AccNumAndAccNum(payerAccountNum, payeeAccountNum)
	        .filter(payee -> payee.getBank().getCode().equals(payeeBankCode))  // Check the bank code
	        .orElseThrow(() -> {
	            logger.error("Payee not found or bank code does not match for accounts: {} and {} with bank code {}", 
	                         payerAccountNum, payeeAccountNum, payeeBankCode);
	            throw new PayeeNotRegisteredException(requestId);
	        });
	}

	@Override
	@Transactional
	public synchronized Account creditToAccount(Account account, BigDecimal amount,UUID requestId) {
		BigDecimal balance = account.getBalance().add(amount);
		return this.setBalance(account, balance);
	}

	@Override
	@Transactional
	public synchronized Account debitFromAccount(Account account, BigDecimal amount,UUID requestId) {
		BigDecimal balance = account.getBalance().subtract(amount);
		 if (account.getBalance().compareTo(amount) < 0) {
	            logger.error("Debit failed:  account has insufficient funds. Account balance is {}", account.getBalance());
	            throw new InsufficientFundsException(requestId);
	        }
		return this.setBalance(account, balance);
	}
	
	/**
	 * Sets balance to account
	 * @param account
	 * @param balance
	 */
	private Account setBalance(Account account, BigDecimal balance ) {
		// Update the balance
		account.setBalance(balance);
		logger.info("Updating balance to '{}'.", balance);
		// Save the updated account back to the database
		return accRepo.save(account);
	}

}
