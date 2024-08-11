package com.exercise.banking.service.transfer.money.service.impl;

import java.math.BigDecimal;

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
public class InterBankTransferService extends  AbstractTransferServiceImpl{

	private static final Logger logger = LoggerFactory.getLogger(InterBankTransferService.class);

	public InterBankTransferService(AccountRepository accountRepository, TransactionRepository txnRepository, AccountService accountService) {
		super(accountRepository, txnRepository, accountService);
	}

	@Override
	@Transactional
	public synchronized Transaction executeTransfer(Account payerAccount, String payeeAccountNum, BigDecimal amount,
			Payee payee) throws AccountNotFoundException {

		logger.info("Executing intra-bank transfer");
		Transaction txn = recordTransaction(payerAccount, payee, amount,TransferType.INTER_BANK_TRANSFER);

		// TODO call the other bank service API
		logger.info("Calling external bank API  ");

		logger.info("Txn: {} Transfer of {} completed successfully from Payer accountto Payee account",txn.getTransactionId(), amount);

		return txn;
	}


}
