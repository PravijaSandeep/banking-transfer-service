package com.exercise.banking.service.transfer.service.impl;

import java.math.BigDecimal;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.exercise.banking.service.transfer.dto.TransferRequestV1;
import com.exercise.banking.service.transfer.exception.AccountNotFoundException;
import com.exercise.banking.service.transfer.model.Account;
import com.exercise.banking.service.transfer.model.Payee;
import com.exercise.banking.service.transfer.model.Transaction;
import com.exercise.banking.service.transfer.model.TransferType;
import com.exercise.banking.service.transfer.service.AccountService;
import com.exercise.banking.service.transfer.service.TransactionService;
/**
 * Service class handles intra bank transfers
 */
@Service
public class IntraBankTransferService extends AbstractTransferServiceImpl {

	private static final Logger logger = LoggerFactory.getLogger(IntraBankTransferService.class);

	public IntraBankTransferService(TransactionService txnService,AccountService accountService) {
		super(txnService, accountService);
	}

	@Override
	protected synchronized Transaction executeTransfer(Account payerAccount, Payee payee, TransferRequestV1 request) throws AccountNotFoundException {
		logger.debug("Trying to execute intra-bank transfer");
		Transaction txn = recordTransaction(payerAccount, payee, request,TransferType.INTRA_BANK_TRANSFER);
		logger.info("Txn: {} Transfer of {} completed successfully.", txn.getTransactionId(),request.getAmount());
		return txn;
	}

	/**
	 * Credit the amount to Payee's account
	 * @param payeeAccountNum
	 * @param amount
	 * @return
	 */
	private  void updatePayeeBalance( String payeeAccNum, BigDecimal amount,UUID requestId) {
		// Add the transfered amount to the payee's account balance
		Account payeeAccount = getPayeeAccount(payeeAccNum,requestId);
		this.accountService.creditToAccount(payeeAccount, amount, requestId);
		logger.info("Payee Account balance updated to {}", payeeAccount.getBalance());
	}

	/**
	 * 
	 * @param payeeAccountNum
	 * @param requestId 
	 * @return
	 */
	private Account getPayeeAccount(String payeeAccountNum, UUID requestId) {
		logger.debug("Getting Payee account details ");
		return this.accountService.getAccountByNumberOrThrow(payeeAccountNum, requestId);
		
	}
	
	@Override
	protected Transaction recordTransaction(Account payerAccount, Payee payee, TransferRequestV1 request,TransferType type) {
		Transaction txn= super.recordTransaction(payerAccount, payee, request, type);
		// update Payee account balance
		this.updatePayeeBalance(request.getPayeeAccNumber(), request.getAmount(), request.getRequestId());
		return txn;
	}

}
