package com.exercise.banking.service.transfer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
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
import com.exercise.banking.service.transfer.repository.AccountRepository;
import com.exercise.banking.service.transfer.repository.TransactionRepository;
import com.exercise.banking.service.transfer.service.impl.AccountServiceImpl;
import com.exercise.banking.service.transfer.service.impl.InterBankTransferService;
import com.exercise.banking.service.transfer.service.impl.IntraBankTransferService;

@Transactional
@DataJpaTest
class SuccessTransferServiceTest {

	@Mock
	private AccountRepository accRepo;

	@Mock
	private TransactionRepository txnRepo;

	@Mock
	private AccountServiceImpl accountService;

	private TransferServiceSelector selector;

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		IntraBankTransferService intraBankSvc = new IntraBankTransferService(accRepo, txnRepo, accountService);
		InterBankTransferService interBankSvc = new InterBankTransferService(accRepo, txnRepo, accountService);
		selector = new TransferServiceSelector(interBankSvc, intraBankSvc);
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

		

		UUID requestId = UUID.randomUUID();
		Account payeeAccount = new Account(payeeAccNumber, new BigDecimal("200.00"), "Payee1", payeeBank, null);
		TransferRequestV1 request = new TransferRequestV1(requestId, payerAccNumber, payeeAccNumber, payeeBankName,
				payeeBankCode, amount, currency, Instant.now().toString());

		// Mock repository and service responses
		when(accRepo.findById(payerAccNumber)).thenReturn(Optional.of(payerAccount));
		when(accRepo.findById(payeeAccNumber)).thenReturn(Optional.of(payeeAccount));
		when(accountService.getPayeeByAccountNumbersOrThrow(payerAccNumber, payeeAccNumber, payeeBankCode, requestId))
				.thenReturn(payee1);

		// Mock the save behavior of the transaction repository
		when(txnRepo.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

		// Perform transfer
		CompletableFuture<TransferResponseV1> future = CompletableFuture.supplyAsync(() -> svc.performTransferV1(request));
		TransferResponseV1 response = future.get();

		// Assert that the transfer was successful
		assertEquals("SUCCESS", response.getStatus());
		assertEquals(expPayerBalance, response.getBalance());
		assertEquals(expTransferType.getValue(), response.getTransferType());
	}
}
