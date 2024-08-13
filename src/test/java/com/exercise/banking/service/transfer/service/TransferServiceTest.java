package com.exercise.banking.service.transfer.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

import com.exercise.banking.service.transfer.dto.TransferRequestV1;
import com.exercise.banking.service.transfer.dto.TransferResponseV1;
import com.exercise.banking.service.transfer.exception.AccountNotFoundException;
import com.exercise.banking.service.transfer.exception.InsufficientFundsException;
import com.exercise.banking.service.transfer.exception.PayeeNotRegisteredException;
import com.exercise.banking.service.transfer.exception.TransactionProcessingException;
import com.exercise.banking.service.transfer.model.Account;
import com.exercise.banking.service.transfer.model.Bank;
import com.exercise.banking.service.transfer.model.Payee;
import com.exercise.banking.service.transfer.model.Transaction;
import com.exercise.banking.service.transfer.model.TransactionStatus;
import com.exercise.banking.service.transfer.model.TransferType;
import com.exercise.banking.service.transfer.repository.AccountRepository;
import com.exercise.banking.service.transfer.repository.TransactionRepository;
import com.exercise.banking.service.transfer.service.impl.AccountServiceImpl;
import com.exercise.banking.service.transfer.service.impl.InterBankTransferService;
import com.exercise.banking.service.transfer.service.impl.IntraBankTransferService;

@Transactional
@DataJpaTest

class TransferServiceTest {
	
	private static final Logger logger = LoggerFactory.getLogger(TransferServiceTest.class);


	@Mock
	AccountRepository accRepo;

	@Mock
	TransactionRepository txnRepo;

	@InjectMocks
	TransferServiceSelectorTest transferSvcSelector;

	private IntraBankTransferService intraBankTransferService;


	private InterBankTransferService interBankTransferService;

	@Mock
	private AccountServiceImpl accountService;
	
	private UUID requestId;
	
	

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);
       intraBankTransferService = new IntraBankTransferService(accRepo, txnRepo, accountService);
       interBankTransferService = new InterBankTransferService(accRepo, txnRepo, accountService);
       
       requestId = UUID.randomUUID();
       MDC.put("requestId", requestId.toString());

	}

	@Test
    void testIntraBankTransfer() {
		
		logger.info("#######TEST FOR INTRA BANK TRANSFER##########");
        // Setup bank, accounts, and payee
        Bank testBank1 = new Bank();
        testBank1.setCode("testCode1");
        testBank1.setName("testBank1");

        Account payerAccount = new Account("ACC001", new BigDecimal("1000.00"), "Payer1", testBank1, new HashSet<>());
        Payee payee1 = new Payee(null, "Person1-Payee1", "ACC002",testBank1, payerAccount);
        payerAccount.addPayee(payee1);
        
        Account payeeAccount = new Account("ACC002", new BigDecimal("200.00"), "Payee1", testBank1, null);
        TransferRequestV1 request = new TransferRequestV1(requestId,"ACC001", "ACC002", "testBank1", "testCode1", new BigDecimal("100.00"),"GBP",Instant.now().toString());

        // Mock repository and service responses
        when(accRepo.findById("ACC001")).thenReturn(Optional.of(payerAccount));
        when(accRepo.findById("ACC002")).thenReturn(Optional.of(payeeAccount));
        when(accountService.getPayeeByAccountNumbersOrThrow("ACC001", "ACC002","testCode1",requestId)).thenReturn(payee1);

        // Mock the save behavior of the transaction repository
        Transaction mockTransaction = createMockTxn(requestId);
        
		mockTransaction.setType(TransferType.INTRA_BANK_TRANSFER.getValue());
		mockTransaction.setCurrency("GBP");
		Account resultPayerAccount = new Account("ACC001", new BigDecimal("900.00"), "Payer1", testBank1, new HashSet<>());
		Payee payee2 = new Payee(null, "Person1-Payee1", "ACC002", testBank1, resultPayerAccount);
		resultPayerAccount.addPayee(payee2);
		
        mockTransaction.setPayerAccount(resultPayerAccount);
        
        when(txnRepo.save(any(Transaction.class))).thenReturn(mockTransaction);

        // Execute the transfer
        TransferResponseV1 response = intraBankTransferService.performTransferV1(request);

        // Assertions
        assertEquals("SUCCESS", response.getStatus());
        assertEquals(requestId,response.getRequestId());
        assertEquals(new BigDecimal("900.00"), response.getBalance());
        assertEquals(new BigDecimal("100.00"), response.getAmount());
        assertEquals(mockTransaction.getTransactionId(), response.getTransactionId());
        assertEquals("GBP", response.getCurrency());
        assertEquals(new BigDecimal("300.00"), payeeAccount.getBalance());
    }
	
	@Test
    void testDuplicateTransfer() {
		
		logger.info("####### TEST TO VERIFY DUPLICATE TRANSFER PROCESSING ##########");
        // Setup bank, accounts, and payee
        Bank testBank1 = new Bank();
        testBank1.setCode("testCode1");
        testBank1.setName("testBank1");

        Account payerAccount = new Account("ACC001", new BigDecimal("1000.00"), "Payer1", testBank1, new HashSet<>());
        Payee payee1 = new Payee(null, "Person1-Payee1", "ACC002",testBank1, payerAccount);
        payerAccount.addPayee(payee1);
        
        Account payeeAccount = new Account("ACC002", new BigDecimal("200.00"), "Payee1", testBank1, null);
        TransferRequestV1 request = new TransferRequestV1(requestId,"ACC001", "ACC002", "testBank1", "testCode1", new BigDecimal("100.00"),"GBP",Instant.now().toString());

        // Mock repository and service responses
        when(accRepo.findById("ACC001")).thenReturn(Optional.of(payerAccount));
        when(accRepo.findById("ACC002")).thenReturn(Optional.of(payeeAccount));
        when(accountService.getPayeeByAccountNumbersOrThrow("ACC001", "ACC002","testCode1",requestId)).thenReturn(payee1);

        // Mock the save behavior of the transaction repository
        Transaction mockTransaction = createMockTxn(requestId);
        
		mockTransaction.setType(TransferType.INTRA_BANK_TRANSFER.getValue());
		mockTransaction.setCurrency("GBP");
		Account resultPayerAccount = new Account("ACC001", new BigDecimal("1000.00"), "Payer1", testBank1, new HashSet<>());
		Payee payee2 = new Payee(null, "Person1-Payee1", "ACC002", testBank1, resultPayerAccount);
		resultPayerAccount.addPayee(payee2);
		
        mockTransaction.setPayerAccount(resultPayerAccount);
        
        when(txnRepo.findByRequestId(requestId)).thenReturn(Optional.of(mockTransaction));

        // Execute the transfer
        TransferResponseV1 response = intraBankTransferService.performTransferV1(request);

        // Assertions
        assertEquals("SUCCESS", response.getStatus());
        assertEquals(requestId,response.getRequestId());
        assertEquals(new BigDecimal("1000.00"), response.getBalance());
        assertEquals(new BigDecimal("100.00"), response.getAmount());
        assertEquals(mockTransaction.getTransactionId(), response.getTransactionId());
        assertEquals("GBP", response.getCurrency());
        assertEquals(new BigDecimal("200.00"), payeeAccount.getBalance());
        assertEquals(new BigDecimal("1000.00"), payerAccount.getBalance());
        assertTrue(response.isDuplicate());
    }
	
	
	@Test
	void testIntraBankTransferFailure() {
	    logger.info("####### TEST FOR INTRA BANK TRANSFER FAILURE ##########");

	    // Setup bank, accounts, and payee
	    Bank testBank1 = new Bank();
        testBank1.setCode("testCode1");
        testBank1.setName("testBank1");
	    Account payerAccount = new Account("ACC001", new BigDecimal("1000.00"), "Payer1", testBank1, new HashSet<>());
	    Payee payee1 = new Payee(null, "Person1-Payee1", "ACC002", testBank1, payerAccount);
	    payerAccount.addPayee(payee1);
	    

	    Account payeeAccount = new Account("ACC002", new BigDecimal("200.00"), "Payee1", testBank1, null);
	    TransferRequestV1 request = new TransferRequestV1(requestId,"ACC001", "ACC002", 
	    		"testBank1", "testCode1", new BigDecimal("100.00"),"EUR",Instant.now().toString());

	    // Mock repository and service responses
	    when(accRepo.findById("ACC001")).thenReturn(Optional.of(payerAccount));
	    when(accRepo.findById("ACC002")).thenReturn(Optional.of(payeeAccount));
	    when(accountService.getPayeeByAccountNumbersOrThrow("ACC001", "ACC002","testCode1",requestId)).thenReturn(payee1);

	    // Mock the save behavior of the transaction repository
	    Transaction mockTransaction = createMockTxn(requestId);
	    mockTransaction.setType(TransferType.INTRA_BANK_TRANSFER.getValue());
	    when(txnRepo.save(any(Transaction.class))).thenReturn(mockTransaction);

	    // Simulate a failure in saving the payer account
	    when(accRepo.save(any(Account.class))).thenThrow(new RuntimeException("Simulated database failure"));

	    // Execute the transfer and expect a TransactionProcessingException
	    assertThrows(TransactionProcessingException.class, () -> intraBankTransferService.performTransferV1(request));

	    // Capture the Transaction object that was passed to save()
	    ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
	    verify(txnRepo, times(1)).save(transactionCaptor.capture()); 

	    // Get the captured Transaction objects
	    List<Transaction> capturedTransactions = transactionCaptor.getAllValues();

	    // Assert that the transaction status was set to FAILURE on the second save
	    Transaction firstTransaction = capturedTransactions.get(0);

	    assertEquals(TransactionStatus.FAILURE, firstTransaction.getStatus());
	    assertEquals(TransferType.INTRA_BANK_TRANSFER.getValue(), firstTransaction.getType());
	}


	@Test
    void testIntraBankTransferWithInvalidPayee() {
		
		logger.info("#######TEST FOR INTRA BANK TRANSFER FOR INVALID PAYEE##########");
        // Setup bank, accounts, and payee
        Bank testBank1 = new Bank();
        testBank1.setCode("testCode1");
        testBank1.setName("testBank1");

        Account payerAccount = new Account("ACC001", new BigDecimal("1000.00"), "Payer1", testBank1, new HashSet<>());
        Payee payee1 = new Payee(null, "Person1-Payee1", "ACC002",testBank1, payerAccount);
        payerAccount.addPayee(payee1);
        
        TransferRequestV1 request = new TransferRequestV1(requestId,"ACC001", "ACC002", "testBank1", "testCode1", 
        		new BigDecimal("100.00"),"GBP",Instant.now().toString());

        // Mock repository and service responses
        when(accRepo.findById("ACC001")).thenReturn(Optional.of(payerAccount));
        when(accRepo.findById("ACC002")).thenReturn(Optional.empty());
        
        when(accountService.getPayeeByAccountNumbersOrThrow("ACC001", "ACC002","testCode1",requestId)).thenReturn(payee1);
        
        assertThrows(AccountNotFoundException.class, () -> intraBankTransferService.performTransferV1(request));
    }

	@Test
	void testInterBankTransfer() {
		
		logger.info("#######TEST FOR INTER BANK TRANSFER##########");


		Bank testBank1 = new Bank();
		testBank1.setCode("testCode1");
		testBank1.setName("testBank1");

		Bank testBank2 = new Bank();
		testBank2.setCode("testCode2");
		testBank2.setName("testBank2");


		Account payerAccount = new Account("ACC001", new BigDecimal("1000.00"), "Payer1", testBank1, new HashSet<>());
		System.out.println(testBank2);

		Payee payee1 = new Payee(null, "Person1-Payee1", "ACC003",  testBank2, payerAccount);
		payerAccount.getPayees().add(payee1);
		
		TransferRequestV1 request = new TransferRequestV1(requestId,"ACC001", "ACC003", "testBank2", "testCode2", 
				new BigDecimal("100.00"),"EUR",Instant.now().toString());

		when(accRepo.findById("ACC001")).thenReturn(Optional.of(payerAccount));
		when(accountService.getPayeeByAccountNumbersOrThrow("ACC001", "ACC003","testCode2",requestId))
		.thenReturn(payee1);
		
		Transaction mockTransaction = createMockTxn(requestId);
		mockTransaction.setType(TransferType.INTER_BANK_TRANSFER.getValue());
		mockTransaction.setCurrency("EUR");
		Account resultPayerAccount = new Account("ACC001", new BigDecimal("900.00"), "Payer1", testBank1, new HashSet<>());
		Payee payee2 = new Payee(null, "Person1-Payee1", "ACC003",   testBank2, resultPayerAccount);
		resultPayerAccount.addPayee(payee2);
		
        mockTransaction.setPayerAccount(resultPayerAccount);

        // Mock the save behavior of the repository
        when(txnRepo.save(any(Transaction.class))).thenReturn(mockTransaction);

		TransferResponseV1 response = interBankTransferService.performTransferV1(request);
		assertEquals(requestId,response.getRequestId());
		assertEquals(new BigDecimal("900.00"),response.getBalance());
		assertEquals(new BigDecimal("100.00"),response.getAmount());
		assertEquals("EUR", response.getCurrency());
		assertEquals("SUCCESS", response.getStatus());

	}

	@Test
	void testTransferToUnknownPayee() {
		
		logger.info("#######TEST FOR TRANSFER TO UNKNWON PAYEE ##########");


		Bank testBank1 = new Bank();
		testBank1.setCode("testCode1");
		testBank1.setName("testBank1");

		Bank testBank2 = new Bank();
		testBank2.setCode("testCode2");
		testBank2.setName("testBank2");

		Account payerAccount = new Account("ACC001", new BigDecimal("1000.00"), "Payer1", testBank1, new HashSet<>());
		TransferRequestV1 request = new TransferRequestV1(requestId,"ACC001", "ACC003", "testBank2", "testCode2", 
				new BigDecimal("100.00"),"GBP",Instant.now().toString());
		
		when(accRepo.findById("ACC001")).thenReturn(Optional.of(payerAccount));
		when(accountService.getPayeeByAccountNumbersOrThrow("ACC001", "ACC003","testCode2",requestId))
        .thenThrow(new PayeeNotRegisteredException(requestId));

		assertThrows(PayeeNotRegisteredException.class, () -> intraBankTransferService.performTransferV1(request));

	}
	
	@Test
	void testTransferWithinSameAccounts() {
		
		logger.info("#######TEST FOR TRANSFER BETWEEN SAME ACCOUNTS ##########");


		Bank testBank1 = new Bank();
		testBank1.setCode("testCode1");
		testBank1.setName("testBank1");

		Account payerAccount = new Account("ACC001", new BigDecimal("1000.00"), "Payer1", testBank1, new HashSet<>());
		TransferRequestV1 request = new TransferRequestV1(UUID.randomUUID(),"ACC001", "ACC001", "testBank1", "testCode1", 
				new BigDecimal("100.00"),"GBP",Instant.now().toString());

		when(accRepo.findById("ACC001")).thenReturn(Optional.of(payerAccount));
		

		assertThrows(IllegalArgumentException.class, () -> intraBankTransferService.performTransferV1(request));

	}


	@Test
	void testTransferFromUnknownAccount() {
		logger.info("#######TEST FOR TRANSFER FROM UNKNWON ACCOUNT ##########");

		TransferRequestV1 request = new TransferRequestV1(UUID.randomUUID(),"ACC001", "ACC003", "testBank2", "testCode2",
				new BigDecimal("100.00"),"GBP",Instant.now().toString());
		assertThrows(AccountNotFoundException.class, () -> intraBankTransferService.performTransferV1(request));
	}

	@Test
	void testTransferWithInSufficientFunds() {
		logger.info("#######TEST FOR TRANSFER WITH INSUFFICIENT FUNDS ##########");

		Bank testBank1 = new Bank();
		testBank1.setCode("testCode1");
		testBank1.setName("testBank1");

		Bank testBank2 = new Bank();
		testBank2.setCode("testCode2");
		testBank2.setName("testBank2");


		Account payerAccount = new Account("ACC001", new BigDecimal("50.00"), "Payer1", testBank1, new HashSet<>());
		System.out.println(testBank2);

		Payee payee1 = new Payee(null, "Person1-Payee1", "ACC003",   testBank2, payerAccount);
		payerAccount.getPayees().add(payee1);

		TransferRequestV1 request = new TransferRequestV1(UUID.randomUUID(),"ACC001", "ACC003", "testBank2", "testCode2", 
				new BigDecimal("100.00"),"GBP",Instant.now().toString());

		when(accRepo.findById("ACC001")).thenReturn(Optional.of(payerAccount));
		assertThrows(InsufficientFundsException.class, () -> intraBankTransferService.performTransferV1(request));

	}
	
	private Transaction createMockTxn(UUID requestId) {
		Transaction mockTransaction = new Transaction();
        mockTransaction.setTransactionId(UUID.randomUUID()); 
        mockTransaction.setRequestId(requestId);
        mockTransaction.setAmount(new BigDecimal("100.00"));
        mockTransaction.setStatus(TransactionStatus.SUCCESS);
		return mockTransaction;
	}
	
	@AfterEach
    public void tearDown() {
        // Clear MDC after each test
        MDC.clear();
    }

}
