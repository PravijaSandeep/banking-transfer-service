package com.exercise.banking.service.transfer.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

import com.exercise.banking.service.transfer.dto.TransferRequest;
import com.exercise.banking.service.transfer.dto.TransferResponse;
import com.exercise.banking.service.transfer.model.Account;
import com.exercise.banking.service.transfer.model.Bank;
import com.exercise.banking.service.transfer.model.Payee;
import com.exercise.banking.service.transfer.model.Transaction;
import com.exercise.banking.service.transfer.repository.AccountRepository;
import com.exercise.banking.service.transfer.repository.TransactionRepository;
import com.exercise.banking.service.transfer.service.impl.AccountServiceImpl;
import com.exercise.banking.service.transfer.service.impl.IntraBankTransferService;

@Transactional
@DataJpaTest
class ConcurrentTransferServiceTest {

    @Mock
    private AccountRepository accRepo;

    @Mock
    private TransactionRepository txnRepo;

    @Mock
    private AccountServiceImpl accountService;

    private IntraBankTransferService intraBankTransferService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        intraBankTransferService = new IntraBankTransferService(accRepo, txnRepo, accountService);
    }

    @Test
    void testConcurrentTransfers() throws ExecutionException, InterruptedException {
        // Setup bank, accounts, and payee
        Bank testBank1 = new Bank();
        testBank1.setCode("testCode1");
        testBank1.setName("testBank1");

        Account payerAccount = new Account("ACC001", new BigDecimal("1000.00"), "Payer1", testBank1, new HashSet<>());
        Payee payee1 = new Payee(null, "Person1-Payee1", "ACC002",  testBank1, payerAccount);
        payerAccount.getPayees().add(payee1);

        UUID requestId = UUID.randomUUID();
        Account payeeAccount = new Account("ACC002", new BigDecimal("200.00"), "Payee1", testBank1, null);
        TransferRequest request = new TransferRequest(requestId, "ACC001", "ACC002", "testBank1", "testCode1", new BigDecimal("100.00"), "GBP", Instant.now().toString());

        // Mock repository and service responses
        when(accRepo.findById("ACC001")).thenReturn(Optional.of(payerAccount));
        when(accRepo.findById("ACC002")).thenReturn(Optional.of(payeeAccount));
        when(accountService.getPayeeByAccountNumbersOrThrow("ACC001", "ACC002", "testCode1", requestId)).thenReturn(payee1);

        // Mock the save behavior of the transaction repository
        when(txnRepo.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Run concurrent transfers using CompletableFuture
        CompletableFuture<TransferResponse>[] futures = IntStream.range(0, 10)
                .mapToObj(i -> CompletableFuture.supplyAsync(() -> intraBankTransferService.performTransfer(request)))
                .toArray(CompletableFuture[]::new);

        // Wait for all futures to complete and assert results
        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures);
        allOf.get();  

        assertAll(
                // Assert each response status
                IntStream.range(0, 10)
                        .mapToObj(i -> () -> assertEquals("SUCCESS", futures[i].join().getStatus())) // using method reference for cleaner code
        );

        // Verify final balance after all transfers
        assertAll(
                () -> assertEquals(new BigDecimal("0.00"), payerAccount.getBalance()),
                () -> assertEquals(new BigDecimal("1200.00"), payeeAccount.getBalance())
        );
    }
}
