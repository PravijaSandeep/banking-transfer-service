package com.exercise.banking.service.transfer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

import com.exercise.banking.service.transfer.model.Transaction;
import com.exercise.banking.service.transfer.model.TransactionStatus;
import com.exercise.banking.service.transfer.repository.TransactionRepository;
import com.exercise.banking.service.transfer.service.impl.TransactionServiceImpl;

@Transactional
@DataJpaTest
public class TransactionServiceImplTest {
	
	@Mock
	TransactionRepository mockTxnRepo;
	
	private  TransactionService txnService;
	
private  UUID requestId ;
	
	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		txnService = new TransactionServiceImpl(mockTxnRepo);
		requestId = UUID.randomUUID();
	}
	

	@Test
	void testFindTxnByRequestId() {
		
		UUID txnId = UUID.randomUUID();
		Transaction mockTxn = createMockTxn(requestId,txnId);
		
		when(mockTxnRepo.findByRequestId(requestId)).thenReturn(Optional.of(mockTxn));
		Optional<Transaction> retrievedTxn = txnService.findByRequestId(requestId);
		assertTrue(retrievedTxn.isPresent());
		assertEquals(requestId, retrievedTxn.get().getRequestId());
		assertEquals(txnId, retrievedTxn.get().getTransactionId());
		
	}
	
	@Test
	void testInvalidTxnByRequestId() {
		
		Optional<Transaction> retrievedTxn = txnService.findByRequestId(requestId);
		assertFalse(retrievedTxn.isPresent());
		
	}
	
	@Test
	void testSaveTxn() {
		
		UUID txnId = UUID.randomUUID();
		Transaction mockTxn = createMockTxn(requestId,txnId);
		
		when(mockTxnRepo.save(mockTxn)).thenReturn(mockTxn);
		Transaction retrievedTxn = txnService.saveTransaction(mockTxn);
		assertNotNull(retrievedTxn);
		assertEquals(requestId, retrievedTxn.getRequestId());
		assertEquals(txnId, retrievedTxn.getTransactionId());
		
	}
	
	private Transaction createMockTxn(UUID requestId,UUID txnId) {
		Transaction mockTransaction = new Transaction();
        mockTransaction.setTransactionId(txnId); 
        mockTransaction.setRequestId(requestId);
        mockTransaction.setAmount(new BigDecimal("100.00"));
        mockTransaction.setStatus(TransactionStatus.SUCCESS);
		return mockTransaction;
	}

}
