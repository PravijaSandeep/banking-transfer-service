package com.exercise.banking.service.transfer.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.exercise.banking.service.transfer.dto.TransferRequestV1;
import com.exercise.banking.service.transfer.model.Account;
import com.exercise.banking.service.transfer.model.Payee;
import com.exercise.banking.service.transfer.model.Transaction;
import com.exercise.banking.service.transfer.model.TransferType;
import com.exercise.banking.service.transfer.repository.AccountRepository;
import com.exercise.banking.service.transfer.repository.TransactionRepository;
/**
 * Service class handles inter bank transactions
 */
@Service
public class InterBankTransferService extends  AbstractTransferServiceImpl{

	private static final Logger logger = LoggerFactory.getLogger(InterBankTransferService.class);

	public InterBankTransferService(AccountRepository accountRepository, TransactionRepository txnRepository, AccountServiceImpl accountService) {
		super(accountRepository, txnRepository, accountService);
	}


	@Override
	protected Transaction executeTransfer(Account payerAccount, Payee payee, TransferRequestV1 request) {
		logger.info("Executing intra-bank transfer");
		Transaction txn = recordTransaction(payerAccount, payee,request,TransferType.INTER_BANK_TRANSFER);

		// TODO call the other bank service API
		logger.info("Calling external bank API  ");

		logger.info("Txn: {} Transfer of {} completed successfully from Payer accountto Payee account",txn.getTransactionId(), request.getAmount());

		return txn;
	}


}
