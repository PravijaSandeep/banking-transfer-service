package com.exercise.banking.service.transfer.money.service.impl;

import java.math.BigDecimal;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.exercise.banking.service.transfer.money.exception.AccountNotFoundException;
import com.exercise.banking.service.transfer.money.model.Account;
import com.exercise.banking.service.transfer.money.model.Payee;
import com.exercise.banking.service.transfer.money.model.Transaction;
import com.exercise.banking.service.transfer.money.model.TransferType;
import com.exercise.banking.service.transfer.money.repository.AccountRepository;
import com.exercise.banking.service.transfer.money.repository.TransactionRepository;
import com.exercise.banking.service.transfer.money.service.AccountService;

@Service
public class IntraBankTransferService extends AbstractTransferServiceImpl {

	private static final Logger logger = LoggerFactory.getLogger(IntraBankTransferService.class);

	public IntraBankTransferService(AccountRepository accountRepository, TransactionRepository txnRepository, AccountService accountService) {
		super(accountRepository, txnRepository, accountService);
	}

	@Override
	@Transactional
	public synchronized Transaction executeTransfer(Account payerAccount, String payeeAccountNum, BigDecimal amount, Payee payee, UUID requestId) throws AccountNotFoundException {
		logger.debug("Trying to execute intra-bank transfer");
		Account payeeAccount = getPayeeAccount(payeeAccountNum,requestId);
		Transaction txn = recordTransaction(payerAccount, payee, amount,TransferType.INTRA_BANK_TRANSFER,requestId);
		updatePayeeBalance(payeeAccount, amount);
		logger.info("Txn: {} Payee Account balance updated to {}",txn.getTransactionId(), payeeAccount.getBalance());

		logger.info("Txn: {} Transfer of {} completed successfully.", txn.getTransactionId(),amount);
		return txn;
	}

	/**
	 * Credit the amount to Payee's account
	 * @param payeeAccountNum
	 * @param amount
	 * @return
	 */
	private  Account updatePayeeBalance( Account payeeAccount, BigDecimal amount) {
		// Add the transfer amount to the payee's account balance
		payeeAccount.setBalance(payeeAccount.getBalance().add(amount));
		accountRepository.save(payeeAccount);

		return payeeAccount;
	}

	/**
	 * 
	 * @param payeeAccountNum
	 * @param requestId 
	 * @return
	 */
	private Account getPayeeAccount(String payeeAccountNum, UUID requestId) {
		logger.debug("Getting Payee account details ");
		return accountRepository.findById(payeeAccountNum)
				.orElseThrow(() ->{ 
					logger.error("Transfer failed for request {}: Unable to find Payee account",requestId);
					throw new AccountNotFoundException(requestId);
				});
	}

}
