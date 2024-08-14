package com.exercise.banking.service.transfer.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import com.exercise.banking.service.transfer.service.impl.IntraBankTransferService;

@Transactional
@DataJpaTest
class ConcurrentTransferServiceTest {


    @Mock
    private AccountService accountService;
    
    @Mock
    TransactionService txnService;

    private IntraBankTransferService intraBankTransferService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        intraBankTransferService = new IntraBankTransferService(txnService, accountService);
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
        TransferRequestV1 request = new TransferRequestV1(requestId, "ACC001", "ACC002", "testBank1", "testCode1", new BigDecimal("100.00"), "GBP", Instant.now().toString());

        // Mock repository and service responses
        when(accountService.getAccountByNumberOrThrow("ACC001",requestId)).thenReturn(payerAccount);
        when(accountService.getAccountByNumberOrThrow("ACC002",requestId)).thenReturn(payeeAccount);
        when(accountService.getPayeeByAccountNumbersOrThrow("ACC001", "ACC002", "testCode1", requestId)).thenReturn(payee1);
        
        Mockito.doAnswer(invocation -> {
            Account account = invocation.getArgument(0);
            BigDecimal amount = invocation.getArgument(1);
            account.setBalance(account.getBalance().add(amount));
            return account; // void method, so return null
        }).when(accountService).creditToAccount(any(Account.class), any(BigDecimal.class), any(UUID.class));
        
        
        
        Mockito.doAnswer(invocation -> {
            Account account = invocation.getArgument(0);
            BigDecimal amount = invocation.getArgument(1);
            account.setBalance(account.getBalance().subtract(amount));
            return account; // void method, so return null
        }).when(accountService).debitFromAccount(any(Account.class), any(BigDecimal.class), any(UUID.class));


        // Mock the save behavior of the transaction repository
        when(txnService.saveTransaction(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Run concurrent transfers using CompletableFuture
        CompletableFuture<TransferResponseV1>[] futures = IntStream.range(0, 10)
                .mapToObj(i -> CompletableFuture.supplyAsync(() -> intraBankTransferService.performTransferV1(request)))
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
