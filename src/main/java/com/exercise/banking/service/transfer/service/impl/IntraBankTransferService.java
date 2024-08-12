package com.exercise.banking.service.transfer.service.impl;

import java.math.BigDecimal;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.exercise.banking.service.transfer.dto.TransferRequest;
import com.exercise.banking.service.transfer.exception.AccountNotFoundException;
import com.exercise.banking.service.transfer.model.Account;
import com.exercise.banking.service.transfer.model.Payee;
import com.exercise.banking.service.transfer.model.Transaction;
import com.exercise.banking.service.transfer.model.TransferType;
import com.exercise.banking.service.transfer.repository.AccountRepository;
import com.exercise.banking.service.transfer.repository.TransactionRepository;
/**
 * Service class handles intra bank transfers
 */
@Service
public class IntraBankTransferService extends AbstractTransferServiceImpl {

	private static final Logger logger = LoggerFactory.getLogger(IntraBankTransferService.class);

	public IntraBankTransferService(AccountRepository accountRepository, TransactionRepository txnRepository, AccountServiceImpl accountService) {
		super(accountRepository, txnRepository, accountService);
	}

	@Override
	protected synchronized Transaction executeTransfer(Account payerAccount, Payee payee, TransferRequest request) throws AccountNotFoundException {
		logger.debug("Trying to execute intra-bank transfer");
		BigDecimal amount = request.getAmount();
		Account payeeAccount = getPayeeAccount(request.getPayeeAccNumber(),request.getRequestId());
		Transaction txn = recordTransaction(payerAccount, payee, request,TransferType.INTRA_BANK_TRANSFER);
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
