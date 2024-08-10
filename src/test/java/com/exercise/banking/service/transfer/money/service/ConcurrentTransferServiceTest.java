package com.exercise.banking.service.transfer.money.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

import com.exercise.banking.service.transfer.money.dto.TransferRequest;
import com.exercise.banking.service.transfer.money.dto.TransferResponse;
import com.exercise.banking.service.transfer.money.model.Account;
import com.exercise.banking.service.transfer.money.model.Bank;
import com.exercise.banking.service.transfer.money.model.Payee;
import com.exercise.banking.service.transfer.money.model.PayeeType;
import com.exercise.banking.service.transfer.money.model.Transaction;
import com.exercise.banking.service.transfer.money.repository.AccountRepository;
import com.exercise.banking.service.transfer.money.repository.TransactionRepository;
import com.exercise.banking.service.transfer.money.service.impl.IntraBankTransferService;

@Transactional
@DataJpaTest
class ConcurrentTransferServiceTest {

    @Mock
    private AccountRepository accRepo;

    @Mock
    private TransactionRepository txnRepo;

    @Mock
    private AccountService accountService;

    private IntraBankTransferService intraBankTransferService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        intraBankTransferService = new IntraBankTransferService(accRepo, txnRepo, accountService);
    }

    @Test
    void testConcurrentTransfers() throws Exception {
        // Setup bank, accounts, and payee
        Bank testBank1 = new Bank();
        testBank1.setCode("testCode1");
        testBank1.setName("testBank1");

        Account payerAccount = new Account("ACC001", new BigDecimal("1000.00"), "Payer1", testBank1, new HashSet<>());
        Payee payee1 = new Payee(null, "Person1-Payee1", "ACC002", PayeeType.INTRA_BANK, testBank1, payerAccount);
        payerAccount.getPayees().add(payee1);

        Account payeeAccount = new Account("ACC002", new BigDecimal("200.00"), "Payee1", testBank1, null);
        TransferRequest request = new TransferRequest("ACC001", "ACC002", "testBank1", "testCode1", new BigDecimal("100.00"));

        // Mock repository and service responses
        when(accRepo.findById("ACC001")).thenReturn(Optional.of(payerAccount));
        when(accRepo.findById("ACC002")).thenReturn(Optional.of(payeeAccount));
        when(accountService.getPayeeByAccountNumbersOrThrow("ACC001", "ACC002","testCode1")).thenReturn(payee1);

     // Mock the save behavior of the transaction repository
        when(txnRepo.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction txn = invocation.getArgument(0);
            txn.setId(System.nanoTime());
            return txn;
        });

        // Run concurrent transfers
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        Future<TransferResponse>[] futures = new Future[10];

        for (int i = 0; i < 10; i++) {
            futures[i] = executorService.submit(() -> intraBankTransferService.performTransfer(request));
        }

        // Wait for all threads to complete
        for (Future<TransferResponse> future : futures) {
            TransferResponse response = future.get();
            assertEquals("SUCCESS", response.getStatus());
        }

        // Verify final balance after all transfers
        assertEquals(new BigDecimal("0.00"), payerAccount.getBalance());
        assertEquals(new BigDecimal("1200.00"), payeeAccount.getBalance());

        executorService.shutdown();
    }
}
