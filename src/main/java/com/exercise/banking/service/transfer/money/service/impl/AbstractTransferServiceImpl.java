package com.exercise.banking.service.transfer.money.service.impl;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.exercise.banking.service.transfer.money.dto.TransferRequest;
import com.exercise.banking.service.transfer.money.dto.TransferResponse;
import com.exercise.banking.service.transfer.money.exception.AccountNotFoundException;
import com.exercise.banking.service.transfer.money.exception.InsufficientFundsException;
import com.exercise.banking.service.transfer.money.exception.TransactionProcessingException;
import com.exercise.banking.service.transfer.money.model.Account;
import com.exercise.banking.service.transfer.money.model.Payee;
import com.exercise.banking.service.transfer.money.model.Transaction;
import com.exercise.banking.service.transfer.money.model.TransactionStatus;
import com.exercise.banking.service.transfer.money.model.TransferType;
import com.exercise.banking.service.transfer.money.repository.AccountRepository;
import com.exercise.banking.service.transfer.money.repository.TransactionRepository;
import com.exercise.banking.service.transfer.money.service.AccountService;
import com.exercise.banking.service.transfer.money.service.TransferService;
/**
 * Abstract Service implementation for Transfer.
 * 
 */

public abstract class AbstractTransferServiceImpl implements TransferService {

    private static final Logger logger = LoggerFactory.getLogger(AbstractTransferServiceImpl.class);

    protected final AccountRepository accountRepository;
    protected final TransactionRepository txnRepository;
    protected final AccountService accountService;

    protected AbstractTransferServiceImpl(AccountRepository accountRepository, TransactionRepository txnRepository, AccountService accountService) {
        this.accountRepository = accountRepository;
        this.txnRepository = txnRepository;
        this.accountService = accountService;
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public TransferResponse performTransfer(TransferRequest request) {
        logger.info("Started processing transfer for request: {}", request);
        Account payerAccount = findPayerAccount(request);
        validateRequest(request, payerAccount);
        Payee payee = findRegisteredPayee(request);
        Transaction txn = executeTransfer(payerAccount, request.getPayeeAccNumber(), request.getAmount(), payee);
        return sendResponse(txn);
    }

    /**
     * Validate the request parameters
     *
     * @param request
     * @param payerAccount
     */
    private void validateRequest(TransferRequest request, Account payerAccount) {
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
    private void checkBalance(TransferRequest request, Account payerAccount) {
        if (payerAccount.getBalance().compareTo(request.getAmount()) < 0) {
            logger.error("Transfer failed: Payer account has insufficient funds. Account balance is {}", payerAccount.getBalance());
            throw new InsufficientFundsException("Insufficient funds in account");
        }
    }

    /**
     * Find and returns the registered Payee for the Payer account
     *
     * @param request
     * @return registered payee
     */
    private Payee findRegisteredPayee(TransferRequest request) {
        logger.info("Finding the registered Payee with account num {} for Payer Account {}", request.getPayeeAccNumber(), request.getPayerAccNumber());
        Payee payee = accountService.getPayeeByAccountNumbersOrThrow(request.getPayerAccNumber(), request.getPayeeAccNumber(),request.getPayeeBankCode());
       
        logger.info("Registered Payee for Payer Account  is {}", payee.getName());
        return payee;
    }

    /**
     * Finds the Payer account matching to the request
     *
     * @param request
     * @return
     */
    private Account findPayerAccount(TransferRequest request) {
        return accountRepository.findById(request.getPayerAccNumber())
                .orElseThrow(() -> 
                     new AccountNotFoundException("Account not found")
                );
    }

    /**
     * Persists the transaction.
     *
     * @param payerAccount the payer's account
     * @param payee the payee's account
     * @param amount the amount to transfer
     * @param type the type of transfer (intra-bank or inter-bank)
     * @return the saved Transaction object
     */
    protected Transaction recordTransaction(Account payerAccount, Payee payee, BigDecimal amount, TransferType type) {
        Transaction transaction = createTransaction(payerAccount, payee, amount, type.getValue());

        try {
            // Update the payer account balance
            updatePayerAccountBalance(payerAccount, amount);

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

            throw new TransactionProcessingException("Error processing transaction");
        }
    }

    
	protected Transaction createTransaction(Account payerAccount, Payee payee, BigDecimal amount, String type) {
		// Record the transaction
        return new Transaction(
                null,  // ID will be auto-generated
                payerAccount,
                payee,
                amount,
                null,  // Timestamp will be set automatically
                TransactionStatus.PENDING,
                type
               );
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
    private TransferResponse sendResponse(Transaction txn) {
        
    	return new TransferResponse(
    			 txn.getTransactionId(),               // transactionId
    			 txn.getStatus().name(),                  // status
    			 txn.getPayerAccount().getBalance(),  // balance
    			 txn.getAmount(),
    			 txn.getType()// transferType
         	);

    }

    /**
     * Executes the transfer to Payee account.
     *
     * @param payerAccount
     * @param payeeAccountNum
     * @param amount
     * @param payee
     * @return TransferResponse
     * @throws AccountNotFoundException
     */
    protected abstract Transaction executeTransfer(Account payerAccount, String payeeAccountNum, BigDecimal amount, Payee payee) throws AccountNotFoundException;
}
