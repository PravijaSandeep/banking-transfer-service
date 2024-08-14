package com.exercise.banking.service.transfer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

import com.exercise.banking.service.transfer.dto.TransferRequestV1;
import com.exercise.banking.service.transfer.dto.TransferResponseV1;
import com.exercise.banking.service.transfer.model.Account;
import com.exercise.banking.service.transfer.model.Bank;
import com.exercise.banking.service.transfer.model.Payee;
import com.exercise.banking.service.transfer.model.Transaction;
import com.exercise.banking.service.transfer.model.TransferType;
import com.exercise.banking.service.transfer.service.impl.InterBankTransferService;
import com.exercise.banking.service.transfer.service.impl.IntraBankTransferService;

@Transactional
@DataJpaTest
class SuccessTransferServiceTest {
	
	@Mock
    private AccountService accountService;
    
    @Mock
    TransactionService txnService;


	private TransferServiceSelector selector;

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		// Create the EnumMap with the mocked services
        Map<TransferType, TransferService> services = new EnumMap<>(TransferType.class);
        
        IntraBankTransferService intraBankSvc = new IntraBankTransferService(txnService, accountService);
		InterBankTransferService interBankSvc = new InterBankTransferService(txnService, accountService);
		
        services.put(TransferType.INTER_BANK_TRANSFER, interBankSvc);
        services.put(TransferType.INTRA_BANK_TRANSFER, intraBankSvc);

        // Create the TransferServiceSelector instance
        selector = new TransferServiceSelector(services);
        
	}

	@ParameterizedTest
	@CsvSource({ "ACC001, ACC002, testBank1, testCode1, 1000.00, 100.00, GBP, INTRA_BANK_TRANSFER",
			"ACC001, ACC003, testBank1, testCode1, 900.00, 200.00, GBP, INTRA_BANK_TRANSFER",
			"ACC002, OACC001, testBank2, testCode2, 500.00, 300.00, GBP, INTER_BANK_TRANSFER"})
	void testSuccessTransfers(String payerAccNumber, String payeeAccNumber, String payeeBankName, String payeeBankCode,
			BigDecimal balance, BigDecimal amount, String currency, String type) throws Exception {
		// Setup bank, accounts, and payee
		Bank payerBank = new Bank();
		payerBank.setCode("testCode1");
		payerBank.setName("testBank1");

		Bank payeeBank = new Bank();
		payeeBank.setCode(payeeBankCode.trim());
		payeeBank.setName(payeeBankName.trim());
		
		TransferService svc = selector.getService(payerBank.getCode(), payeeBankCode);

		
		BigDecimal expPayerBalance = balance.subtract(amount);
		TransferType expTransferType = TransferType.valueOf(type);

		Account payerAccount = new Account(payerAccNumber, balance, "Payer1", payerBank, new HashSet<>());
		Payee payee1 = new Payee(null, "Person1-Payee1", payeeAccNumber,payeeBank, payerAccount);
		payerAccount.getPayees().add(payee1);

		Mockito.doAnswer(invocation -> {
            Account account = invocation.getArgument(0);
            BigDecimal amnt = invocation.getArgument(1);
            account.setBalance(account.getBalance().add(amnt));
            return account; // void method, so return null
        }).when(accountService).creditToAccount(any(Account.class), any(BigDecimal.class), any(UUID.class));
        
        Mockito.doAnswer(invocation -> {
            Account account = invocation.getArgument(0);
            BigDecimal amnt = invocation.getArgument(1);
            account.setBalance(account.getBalance().subtract(amnt));
            return account; // void method, so return null
        }).when(accountService).debitFromAccount(any(Account.class), any(BigDecimal.class), any(UUID.class));


		UUID requestId = UUID.randomUUID();
		Account payeeAccount = new Account(payeeAccNumber, new BigDecimal("200.00"), "Payee1", payeeBank, null);
		TransferRequestV1 request = new TransferRequestV1(requestId, payerAccNumber, payeeAccNumber, payeeBankName,
				payeeBankCode, amount, currency, Instant.now().toString());

		// Mock repository and service responses
		when(accountService.getPayeeByAccountNumbersOrThrow(payerAccNumber, payeeAccNumber, payeeBankCode, requestId))
				.thenReturn(payee1);
		
		 when(accountService.getAccountByNumberOrThrow(payerAccNumber,requestId)).thenReturn(payerAccount);
	     when(accountService.getAccountByNumberOrThrow(payeeAccNumber,requestId)).thenReturn(payeeAccount);
	        

		// Mock the save behavior of the transaction repository
	    when(txnService.saveTransaction(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

		// Perform transfer
		CompletableFuture<TransferResponseV1> future = CompletableFuture.supplyAsync(() -> svc.performTransferV1(request));
		TransferResponseV1 response = future.get();

		// Assert that the transfer was successful
		assertEquals("SUCCESS", response.getStatus());
		assertEquals(expPayerBalance, response.getBalance());
		assertEquals(expTransferType.getValue(), response.getTransferType());
	}
}
