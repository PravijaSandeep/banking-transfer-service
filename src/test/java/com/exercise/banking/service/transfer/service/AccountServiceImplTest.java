package com.exercise.banking.service.transfer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

import com.exercise.banking.service.transfer.exception.AccountNotFoundException;
import com.exercise.banking.service.transfer.exception.InsufficientFundsException;
import com.exercise.banking.service.transfer.exception.PayeeNotRegisteredException;
import com.exercise.banking.service.transfer.model.Account;
import com.exercise.banking.service.transfer.model.Bank;
import com.exercise.banking.service.transfer.model.Payee;
import com.exercise.banking.service.transfer.model.Transaction;
import com.exercise.banking.service.transfer.repository.AccountRepository;
import com.exercise.banking.service.transfer.repository.PayeeRepository;
import com.exercise.banking.service.transfer.service.impl.AccountServiceImpl;

@Transactional
@DataJpaTest
class AccountServiceImplTest {
	
	@Mock
	PayeeRepository payeeRepo;
	
	@Mock
	AccountRepository accRepo;
	
	private AccountService accountService;
	
	private  UUID requestId ;
	
	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		accountService = new AccountServiceImpl(accRepo, payeeRepo);
		requestId = UUID.randomUUID();
	}
	
	@Test
	void testRegisteredPayeeRetrieval() {
	    // Setup the bank instance
	    Bank testBank1 = new Bank();
	    testBank1.setCode("testCode1");
	    testBank1.setName("testBank1");

	    // Setup the payer account with an initial balance and link to the bank
	    Account payerAccount = new Account("PAYER001", new BigDecimal("1000.00"), "Payer1", testBank1, new HashSet<>());
	    
	    // Create a Payee and associate it with the payer account
	    Payee payee1 = new Payee(null, "NickName", "PAYEE001", testBank1, payerAccount);
	    payerAccount.getPayees().add(payee1);
	    
	    // Mock the repository call to return the Payee
	    when(payeeRepo.findByPayerAccount_AccNumAndAccNum("PAYER001", "PAYEE001")).thenReturn(Optional.of(payee1));
	    
	    // Call the method under test
	    Payee retrievedPayee = accountService.getPayeeByAccountNumbersOrThrow("PAYER001", "PAYEE001","testCode1",UUID.randomUUID());
	    
	    // Validate the result
	    assertNotNull(retrievedPayee, "The retrieved Payee should not be null");
	    assertEquals("PAYEE001", retrievedPayee.getAccNum(), "The Payee account number should match");
	    assertEquals("NickName", retrievedPayee.getName(), "The Payee nickname should match");
	    assertEquals(testBank1, retrievedPayee.getBank(), "The Payee's bank should match the expected bank");
	}
	
	@Test
	void testAccountRetrieval() {
	    // Setup the bank instance
	    Bank testBank1 = new Bank();
	    testBank1.setCode("testCode1");
	    testBank1.setName("testBank1");

	    // Setup the payer account with an initial balance and link to the bank
	    Account payerAccount = new Account("PAYER001", new BigDecimal("1000.00"), "Payer1", testBank1, new HashSet<>());
	    
	    when(accRepo.findById("PAYER001")).thenReturn(Optional.of(payerAccount));
	    
	    
	    // Call the method under test
	    Account retrieved = accountService.getAccountByNumberOrThrow("PAYER001",UUID.randomUUID());
	    
	    // Validate the result
	    assertNotNull(retrieved, "The retrieved account should not be null");
	    assertEquals("PAYER001", retrieved.getAccNum(), "The  account number should match");
	    assertEquals(new BigDecimal("1000.00"),retrieved.getBalance(), "The balance should match");
	    assertEquals(testBank1, retrieved.getBank(), "The bank should match the expected bank");
	}
	
	@Test
	void testCreditToAccountl() {
	    // Setup the bank instance
	    Bank testBank1 = new Bank();
	    testBank1.setCode("testCode1");
	    testBank1.setName("testBank1");

	    // Setup the payer account with an initial balance and link to the bank
	    Account payerAccount = new Account("PAYER001", new BigDecimal("1000.00"), "Payer1", testBank1, new HashSet<>());
	    
	    when(accRepo.findById("PAYER001")).thenReturn(Optional.of(payerAccount));
	    when(accRepo.save(payerAccount)).thenReturn(payerAccount);
	    
	    BigDecimal amount = BigDecimal.valueOf(200);
	    
	    // Call the method under test
	    Account retrieved = accountService.creditToAccount(payerAccount, amount, requestId);
	    
	    // Validate the result
	    assertNotNull(retrieved, "The retrieved account should not be null");
	    assertEquals("PAYER001", retrieved.getAccNum(), "The  account number should match");
	    assertEquals(new BigDecimal("1200.00"),retrieved.getBalance(), "The balance should match");
	}
	
	@Test
	void testDebitFromAccountl() {
	    // Setup the bank instance
	    Bank testBank1 = new Bank();
	    testBank1.setCode("testCode1");
	    testBank1.setName("testBank1");

	    // Setup the payer account with an initial balance and link to the bank
	    Account payerAccount = new Account("PAYER001", new BigDecimal("1000.00"), "Payer1", testBank1, new HashSet<>());
	    
	    when(accRepo.findById("PAYER001")).thenReturn(Optional.of(payerAccount));
	    when(accRepo.save(payerAccount)).thenReturn(payerAccount);
	    
	    BigDecimal amount = BigDecimal.valueOf(200);
	    
	    // Call the method under test
	    Account retrieved = accountService.debitFromAccount(payerAccount, amount, requestId);
	    
	    // Validate the result
	    assertNotNull(retrieved, "The retrieved account should not be null");
	    assertEquals("PAYER001", retrieved.getAccNum(), "The  account number should match");
	    assertEquals(new BigDecimal("800.00"),retrieved.getBalance(), "The balance should match");
	}
	
	@Test
	void testDebitWithInSufficientFunds() {
	    // Setup the bank instance
	    Bank testBank1 = new Bank();
	    testBank1.setCode("testCode1");
	    testBank1.setName("testBank1");

	    // Setup the payer account with an initial balance and link to the bank
	    Account payerAccount = new Account("PAYER001", new BigDecimal("1000.00"), "Payer1", testBank1, new HashSet<>());
	    
	    when(accRepo.findById("PAYER001")).thenReturn(Optional.of(payerAccount));
	    when(accRepo.save(payerAccount)).thenReturn(payerAccount);
	    
	    BigDecimal amount = BigDecimal.valueOf(1200);
	    
	    // Call the method under test
		assertThrows(InsufficientFundsException.class, () -> accountService.debitFromAccount(payerAccount, amount, requestId));
	}

	@Test
	void testAccountRetrievalFailure() {
	    // Setup the bank instance
	    Bank testBank1 = new Bank();
	    testBank1.setCode("testCode1");
	    testBank1.setName("testBank1");
	   
	    assertThrows(AccountNotFoundException.class, () -> accountService.getAccountByNumberOrThrow("PAYER001", requestId));
	}
	
	@Test
	void testPayeeRetrievalWithInvalidBankCode() {
		// Setup the bank instance
	    Bank testBank1 = new Bank();
	    testBank1.setCode("testCode1");
	    testBank1.setName("testBank1");

	    // Setup the payer account with an initial balance and link to the bank
	    Account payerAccount = new Account("PAYER001", new BigDecimal("1000.00"), "Payer1", testBank1, new HashSet<>());
	    
	    // Create a Payee and associate it with the payer account
	    Payee payee1 = new Payee(null, "NickName", "PAYEE001", testBank1, payerAccount);
	    payerAccount.getPayees().add(payee1);
	    
	    // Mock the repository call to return the Payee
	    when(payeeRepo.findByPayerAccount_AccNumAndAccNum("PAYER001", "PAYEE001")).thenReturn(Optional.of(payee1));
	    
	    
	    assertThrows(PayeeNotRegisteredException.class, () -> {
            accountService.getPayeeByAccountNumbersOrThrow("PAYER001", "PAYEE001","invalidCode",requestId);
        }, "Expected PayeeNotRegisteredException to be thrown when payee is not found");
	}
	
	@Test
    void testUnregisteredPayee() {
        // Arrange
        String payerAccountNum = "PAYER001";
        String payeeAccountNum = "PAYEE_NOT_REGISTERED";
        String payeeBankCode = "PAYEEBANK01";
        
        // Mock the repository to return Optional.empty() for an unregistered payee
        when(payeeRepo.findByPayerAccount_AccNumAndAccNum(payerAccountNum, payeeAccountNum))
            .thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(PayeeNotRegisteredException.class, () -> {
            accountService.getPayeeByAccountNumbersOrThrow(payerAccountNum, payeeAccountNum, payeeBankCode,requestId);
        }, "Expected PayeeNotRegisteredException to be thrown when payee is not found");
    }
	

}
