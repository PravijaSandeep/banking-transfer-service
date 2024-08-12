package com.exercise.banking.service.transfer.service.impl;

import java.math.BigDecimal;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import com.exercise.banking.service.transfer.dto.TransferRequestV1;
import com.exercise.banking.service.transfer.dto.TransferResponseV1;
import com.exercise.banking.service.transfer.exception.AccountNotFoundException;
import com.exercise.banking.service.transfer.exception.InsufficientFundsException;
import com.exercise.banking.service.transfer.exception.TransactionProcessingException;
import com.exercise.banking.service.transfer.model.Account;
import com.exercise.banking.service.transfer.model.Payee;
import com.exercise.banking.service.transfer.model.Transaction;
import com.exercise.banking.service.transfer.model.TransactionStatus;
import com.exercise.banking.service.transfer.model.TransferType;
import com.exercise.banking.service.transfer.repository.AccountRepository;
import com.exercise.banking.service.transfer.repository.TransactionRepository;
import com.exercise.banking.service.transfer.service.TransferService;
/**
 * Abstract Service implementation for Transfer.
 * 
 */

public abstract class AbstractTransferServiceImpl implements TransferService {

    private static final Logger logger = LoggerFactory.getLogger(AbstractTransferServiceImpl.class);

    protected final AccountRepository accountRepository;
    protected final TransactionRepository txnRepository;
    protected final AccountServiceImpl accountService;

    protected AbstractTransferServiceImpl(AccountRepository accountRepository, TransactionRepository txnRepository, AccountServiceImpl accountService) {
        this.accountRepository = accountRepository;
        this.txnRepository = txnRepository;
        this.accountService = accountService;
    }

    @Override
    @Transactional
    public TransferResponseV1 performTransferV1(TransferRequestV1 request) {
        logger.info("Started processing transfer for request");
        Account payerAccount = findPayerAccount(request);
        validateRequest(request, payerAccount);
        Payee payee = findRegisteredPayee(request);
        Transaction txn = executeTransfer(payerAccount, payee, request);
        return sendResponse(txn);
    }

    /**
     * Validate the request parameters
     *
     * @param request
     * @param payerAccount
     */
    private void validateRequest(TransferRequestV1 request, Account payerAccount) {
        if (request.getPayerAccNumber().equals(request.getPayeeAccNumber())) {
            throw new IllegalArgumentException("Payer and payee accounts cannot be the same.");
        }
        checkBalance(request, payerAccount);
    }

    /**
     * Check if the payer account has enough balance to proceed with transfer
     *
     * @param request
     * @param payerAccount
     */
    private void checkBalance(TransferRequestV1 request, Account payerAccount) {
        if (payerAccount.getBalance().compareTo(request.getAmount()) < 0) {
            logger.error("Transfer failed: Payer account has insufficient funds. Account balance is {}", payerAccount.getBalance());
            throw new InsufficientFundsException(request.getRequestId());
        }
    }

    /**
     * Find and returns the registered Payee for the Payer account
     *
     * @param request
     * @return registered payee
     */
    private Payee findRegisteredPayee(TransferRequestV1 request) {
        logger.info("Finding the registered Payee");
        Payee payee = accountService.getPayeeByAccountNumbersOrThrow(request.getPayerAccNumber(), request.getPayeeAccNumber(),request.getPayeeBankCode(),request.getRequestId());
        logger.debug("Registered Payee found is {}", payee.getName());
        return payee;
    }

    /**
     * Finds the Payer account matching to the request
     *
     * @param request
     * @return
     */
    private Account findPayerAccount(TransferRequestV1 request) {
        return accountRepository.findById(request.getPayerAccNumber())
                .orElseThrow(() -> 
                     new AccountNotFoundException(request.getRequestId())
                );
    }

    /**
     * Persists the transaction.
     *
     * @param payerAccount the payer's account
     * @param payee the payee's account
     * @param amount the amount to transfer
     * @param type the type of transfer (intra-bank or inter-bank)
     * @param requestId 
     * @return the saved Transaction object
     */
    protected Transaction recordTransaction(Account payerAccount, Payee payee, TransferRequestV1 request,TransferType type) {
        Transaction transaction = createTransaction(payerAccount, payee, request, type.getValue());
        try {
            // Update the payer account balance
            updatePayerAccountBalance(payerAccount, request.getAmount());

            // Save the transaction with status "Success"
            transaction.setStatus(TransactionStatus.SUCCESS);
            transaction = txnRepository.save(transaction);

            logger.info("Transaction {} recorded successfully", transaction.getTransactionId());
            return transaction;

        } catch (Exception e) {
            // If any error occurs, log the exception
            logger.error("Transaction failed due to exception {}", e.getMessage(), e);

            // Set the transaction status to "Failure"
            transaction.setStatus(TransactionStatus.FAILURE);
            txnRepository.save(transaction); // Save the failed transaction

            throw new TransactionProcessingException(request.getRequestId(),e.getMessage());
        }
    }
    
    /**
     * Creates the Transaction for the request
     * @param payerAccount
     * @param payee
     * @param request
     * @param type
     * @return
     */
	protected Transaction createTransaction(Account payerAccount, Payee payee, TransferRequestV1 request,String type) {
		// Record the transaction
		 Transaction txn = new Transaction(
                null,  // ID will be auto-generated
                request.getRequestId(),
                payerAccount,
                payee,
                request.getAmount(),
                request.getCurrency(),
                null,  // Timestamp will be set automatically
                TransactionStatus.PENDING,
                type
               );
		 
		 logger.info("Request txn : {}", txn.getCurrency());
		 return txn;
	}

    /**
     * Updates Payer Account balance
     * @param payerAccount
     * @param amount
     */
	private synchronized void updatePayerAccountBalance(Account payerAccount, BigDecimal amount) {
		logger.info("Debiting {} from payer Account with balance: {}", amount,  payerAccount.getBalance());
        BigDecimal balance = payerAccount.getBalance().subtract(amount);
        logger.info("Updating balance to '{}'.", balance);

        payerAccount.setBalance(balance);
        accountRepository.save(payerAccount);
	}
	
	 /**
     * Sends the response for Transfer request.
     *
     * @param payerBalance
     * @param transferredAmount
     * @return
     */
    private TransferResponseV1 sendResponse(Transaction txn) {
        
    	return new TransferResponseV1.Builder()
    		    .withRequestId(txn.getRequestId())  // original request id
    		    .withTransactionId(txn.getTransactionId())  // transactionId
    		    .withStatus(txn.getStatus().name())  // status
    		    .withBalance(txn.getPayerAccount().getBalance())  // balance
    		    .withAmount(txn.getAmount())
    		    .withCurrency(txn.getCurrency())
    		    .withTransferType(txn.getType())  // transferType
    		    .withTimestamp(Instant.now())  // current timestamp
    		    .build();

    }

    /**
     * Execute the transfer based on transfer type identified
     * @param payerAccount
     * @param payee
     * @param request
     * @return Completed Transaction
     */
    protected abstract Transaction executeTransfer(Account payerAccount, Payee payee, TransferRequestV1 request);
}
