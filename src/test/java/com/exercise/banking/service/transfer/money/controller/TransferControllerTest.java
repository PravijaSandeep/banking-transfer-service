package com.exercise.banking.service.transfer.money.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.exercise.banking.service.transfer.money.config.BankConfiguration;
import com.exercise.banking.service.transfer.money.dto.TransferRequest;
import com.exercise.banking.service.transfer.money.dto.TransferResponse;
import com.exercise.banking.service.transfer.money.exception.AccountNotFoundException;
import com.exercise.banking.service.transfer.money.exception.InsufficientFundsException;
import com.exercise.banking.service.transfer.money.exception.PayeeNotRegisteredException;
import com.exercise.banking.service.transfer.money.service.TransferService;
import com.exercise.banking.service.transfer.money.service.TransferServiceSelector;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(TransferController.class)
class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransferServiceSelector transferServiceSelector;

    @MockBean
    private BankConfiguration bankConfiguration;

    @Mock
    private TransferService transferService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Mocking the behavior of BankConfiguration
        when(bankConfiguration.getBankCode()).thenReturn("BANK123");

        // Mocking the behavior of TransferServiceSelector
        when(transferServiceSelector.getService(any(String.class), any(String.class)))
                .thenReturn(transferService);
    }

    @Test
    void testSuccessTransfer() throws Exception {
        // Given
        TransferRequest transferRequest = new TransferRequest(
                "payerAccNumber123",
                "payeeAccNumber456",
                "Payee Bank",
                "PayeeBankCode",
                BigDecimal.valueOf(100.00)
        );

        TransferResponse transferResponse = new TransferResponse();
        transferResponse.setTransactionId("transactionId123");
        transferResponse.setStatus("SUCCESS");
        transferResponse.setBalance(BigDecimal.valueOf(900.00));
        transferResponse.setAmount(BigDecimal.valueOf(100.00));


        // Mock the behavior of performTransfer
        when(transferService.performTransfer(any(TransferRequest.class)))
                .thenReturn(transferResponse);

        // When & Then
        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(transferRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.transactionId").value("transactionId123"))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.balance").value(900.00))
                .andExpect(jsonPath("$.amount").value(100.00));
    }
    
    @Test
    void testValidationFailure() throws Exception {
        // Given
        TransferRequest transferRequest = new TransferRequest(
                "payerAccNumber123",
                "payeeAccNumber456",
                null,
                null,
                BigDecimal.valueOf(100.00)
        );


        // When & Then
        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(transferRequest)))
                .andExpect(status().is4xxClientError());
    }
    
    @Test
    void testTransferWithInvalidAccount() throws Exception {
    	
    	String nonExistentAccountNumber = "ACC999";
    	TransferRequest transferRequest = new TransferRequest(
                "payerAccNumber123",
                "payeeAccNumber456",
                "Payee Bank",
                "PayeeBankCode",
                BigDecimal.valueOf(100.00)
        );


    	// Mock the behavior of performTransfer
         when(transferService.performTransfer(any(TransferRequest.class))).thenThrow(new AccountNotFoundException(nonExistentAccountNumber));

      

         // Then: perform the request and expect a 404 response with the error message
         mockMvc.perform(post("/api/transfers")
                 .contentType(MediaType.APPLICATION_JSON)
                 .content(asJsonString(transferRequest)))
             	 .andExpect(status().isNotFound())
             	 .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.name()))
             	 .andExpect(jsonPath("$.message").value("Account not found with ID: " + nonExistentAccountNumber));
     }
    
    @Test
    void testTransferFromInSufficientFund() throws Exception {
    	
    	String accNum = "ACC999";
    	TransferRequest transferRequest = new TransferRequest(
                "payerAccNumber123",
                "payeeAccNumber456",
                "Payee Bank",
                "PayeeBankCode",
                BigDecimal.valueOf(100.00)
        );


    	// Mock the behavior of performTransfer
         when(transferService.performTransfer(any(TransferRequest.class))).thenThrow(new InsufficientFundsException(accNum));

      

         // Then: perform the request and expect a 404 response with the error message
         mockMvc.perform(post("/api/transfers")
                 .contentType(MediaType.APPLICATION_JSON)
                 .content(asJsonString(transferRequest)))
             	 .andExpect(status().isBadRequest())
             	 .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.name()))
             	 .andExpect(jsonPath("$.message").value("Insufficient funds in account " + accNum));
     }
    
    
    @Test
    void testTransferToUnknownPayee() throws Exception {
    	
    	String payeeAccNum = "ACC999";
    	String payerAccNum = "ACC888";
    	TransferRequest transferRequest = new TransferRequest(
                "payerAccNumber123",
                "payeeAccNumber456",
                "Payee Bank",
                "PayeeBankCode",
                BigDecimal.valueOf(100.00)
        );


    	// Mock the behavior of performTransfer
         when(transferService.performTransfer(any(TransferRequest.class))).thenThrow(new PayeeNotRegisteredException(payeeAccNum,payerAccNum));

      

         // Then: perform the request and expect a 404 response with the error message
         mockMvc.perform(post("/api/transfers")
                 .contentType(MediaType.APPLICATION_JSON)
                 .content(asJsonString(transferRequest)))
             	 .andExpect(status().isNotFound())
             	 .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.name()))
             	 .andExpect(jsonPath("$.message").value("Transfer failed: Payee with account number '"+ payeeAccNum +"' is not registered by Payer '" + payerAccNum +"'."));
     }
    
    @Test
    void testTransferGeneralException() throws Exception {
    	
    	TransferRequest transferRequest = new TransferRequest(
                "payerAccNumber123",
                "payeeAccNumber456",
                "Payee Bank",
                "PayeeBankCode",
                BigDecimal.valueOf(100.00)
        );


    	// Mock the behavior of performTransfer
         when(transferService.performTransfer(any(TransferRequest.class))).thenThrow(new RuntimeException("Unknown Error"));

      

         // Then: perform the request and expect a 404 response with the error message
         mockMvc.perform(post("/api/transfers")
                 .contentType(MediaType.APPLICATION_JSON)
                 .content(asJsonString(transferRequest)))
             	 .andExpect(status().is5xxServerError())
             	 .andExpect(jsonPath("$.status").value(HttpStatus.INTERNAL_SERVER_ERROR.name()))
             	 .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
     }


    // Helper method to convert an object to a JSON string
    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
