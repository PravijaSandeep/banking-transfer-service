package com.exercise.banking.service.transfer.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.exercise.banking.service.transfer.config.BankConfiguration;
import com.exercise.banking.service.transfer.dto.TransferRequest;
import com.exercise.banking.service.transfer.dto.TransferResponse;
import com.exercise.banking.service.transfer.exception.AccountNotFoundException;
import com.exercise.banking.service.transfer.exception.InsufficientFundsException;
import com.exercise.banking.service.transfer.exception.PayeeNotRegisteredException;
import com.exercise.banking.service.transfer.model.TransferType;
import com.exercise.banking.service.transfer.service.TransferService;
import com.exercise.banking.service.transfer.service.TransferServiceSelector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@WebMvcTest(TransferController.class)
class TransferControllerTest {
	
	private static final Logger logger = LoggerFactory.getLogger(TransferControllerTest.class);

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransferServiceSelector transferServiceSelector;

    @MockBean
    private BankConfiguration bankConfiguration;

    @Mock
    private TransferService transferService;
    
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Mocking the behavior of BankConfiguration
        when(bankConfiguration.getBankCode()).thenReturn("BANK123");

        // Mocking the behavior of TransferServiceSelector
        when(transferServiceSelector.getService(any(String.class), any(String.class)))
                .thenReturn(transferService);
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.findAndRegisterModules(); // This will register all available modules, including JavaTimeModule

    }

    @Test
    void testSuccessTransfer() throws Exception {
        // Given
    	UUID requestid = UUID.randomUUID();
        TransferRequest transferRequest = new TransferRequest(
        		requestid,
                "1234567",
                "987654",
                "Payee Bank",
                "BANK01",
                BigDecimal.valueOf(100.00),
                "GBP",
                Instant.now().toString()
        );
        
        UUID txnId = UUID.randomUUID();
        
        logger.info("TxnId: {}",txnId);
        logger.info("RequestId: {}",requestid);

        TransferResponse transferResponse = new TransferResponse.Builder()
        	    .withRequestId(requestid)  
        	    .withTransactionId(txnId)  
        	    .withStatus("SUCCESS")  
        	    .withBalance(BigDecimal.valueOf(900.00))  
        	    .withAmount( BigDecimal.valueOf(100.00))
        	    .withCurrency("GBP")
        	    .withTransferType(TransferType.INTRA_BANK_TRANSFER.getValue())  // transferType
        	    .withTimestamp(Instant.now())  
        	    .build();


        // Mock the behavior of performTransfer
        when(transferService.performTransfer(any(TransferRequest.class)))
                .thenReturn(transferResponse);

        // When & Then
        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(transferRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.transactionId").value(txnId.toString()))
                .andExpect(jsonPath("$.requestId").value(requestid.toString()))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.balance").value(900.00))
                .andExpect(jsonPath("$.amount").value(100.00));
    }
    
    @Test
    void testValidationFailure() throws Exception {
        // Given
        TransferRequest transferRequest = new TransferRequest(
        		UUID.randomUUID(),
                "1234567",
                "987654",
                null,
                null,
                BigDecimal.valueOf(100.00),
                "GBP",
                Instant.now().toString()
        );


        // When & Then
        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(transferRequest)))
                .andExpect(status().is4xxClientError());
    }
    
    @Test
    void testTransferWithInvalidAccount() throws Exception {
    	
    	UUID requestID = UUID.randomUUID();
    	TransferRequest transferRequest = new TransferRequest(
    			UUID.randomUUID(),
                "1234567",
                "987654",
                "Payee Bank",
                "BANK01",
                BigDecimal.valueOf(100.00),
                "GBP",
                Instant.now().toString()
        );


    	// Mock the behavior of performTransfer
         when(transferService.performTransfer(any(TransferRequest.class))).thenThrow(new AccountNotFoundException(requestID));

      

         // Then: perform the request and expect a 404 response with the error message
         mockMvc.perform(post("/api/transfers")
                 .contentType(MediaType.APPLICATION_JSON)
                 .content(asJsonString(transferRequest)))
             	 .andExpect(status().isNotFound())
             	 .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.name()))
             	 .andExpect(jsonPath("$.message").value("Account not found"));
     }
    
    @Test
    void testTransferFromInSufficientFund() throws Exception {
    	
    	UUID requestID = UUID.randomUUID();
    	TransferRequest transferRequest = new TransferRequest(
    			requestID,
                "1234567",
                "987654",
                "Payee Bank",
                "BANK01",
                BigDecimal.valueOf(100.00),
                "GBP",
                Instant.now().toString()
        );


    	// Mock the behavior of performTransfer
         when(transferService.performTransfer(any(TransferRequest.class))).thenThrow(new InsufficientFundsException(requestID));

      

         // Then: perform the request and expect a 404 response with the error message
         mockMvc.perform(post("/api/transfers")
                 .contentType(MediaType.APPLICATION_JSON)
                 .content(asJsonString(transferRequest)))
             	 .andExpect(status().isBadRequest())
             	 .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.name()))
             	 .andExpect(jsonPath("$.message").value("Insufficient funds in account"));
     }
    
    
    @Test
    void testTransferToUnknownPayee() throws Exception {
    	
    	UUID requestID = UUID.randomUUID();
    	TransferRequest transferRequest = new TransferRequest(
    			requestID,
                "1234567",
                "987654",
                "Payee Bank",
                "BANK01",
                BigDecimal.valueOf(100.00),
                "GBP",
                Instant.now().toString()
        );

    	// Mock the behavior of performTransfer
         when(transferService.performTransfer(any(TransferRequest.class))).thenThrow(new PayeeNotRegisteredException(requestID));

         // Then: perform the request and expect a 404 response with the error message
         mockMvc.perform(post("/api/transfers")
                 .contentType(MediaType.APPLICATION_JSON)
                 .content(asJsonString(transferRequest)))
             	 .andExpect(status().isNotFound())
             	 .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.name()))
             	 .andExpect(jsonPath("$.message").value("Payee not registered"));
     }
    
    @Test
    void testTransferWithInvalidCurrency() throws Exception {
    	
    	UUID requestID = UUID.randomUUID();
    	TransferRequest transferRequest = new TransferRequest(
    			requestID,
                "1234567",
                "987654",
                "Payee Bank",
                "BANK01",
                BigDecimal.valueOf(100.00),
                "TEST_CURRENCY",
                Instant.now().toString()
        );


    	// Mock the behavior of performTransfer
         when(transferService.performTransfer(any(TransferRequest.class))).thenThrow(new PayeeNotRegisteredException(requestID));

      

         // Then: perform the request and expect a 404 response with the error message
         mockMvc.perform(post("/api/transfers")
                 .contentType(MediaType.APPLICATION_JSON)
                 .content(asJsonString(transferRequest)))
             	 .andExpect(status().isBadRequest())
             	 .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.name()))
             	 .andExpect(jsonPath("$.message").value("Validation failed for input data"))
             	.andExpect(jsonPath("$.validationErrors.currency").value("Currency must be one of the following:  EUR, GBP"));
     }
    
    @Test
    void testTransferWithOutOfRangeValues() throws Exception {
    	
    	UUID requestID = UUID.randomUUID();
    	TransferRequest transferRequest = new TransferRequest(
    			requestID,
                "123",
                "9876543218956432",
                "Payee Bank",
                "BANK00001",
                BigDecimal.valueOf(0.00),
                "TEST_CURRENCY",
                Instant.now().toString()
        );


    	// Mock the behavior of performTransfer
         when(transferService.performTransfer(any(TransferRequest.class))).thenThrow(new PayeeNotRegisteredException(requestID));

         // Then: perform the request and expect a 404 response with the error message
         mockMvc.perform(post("/api/transfers")
                 .contentType(MediaType.APPLICATION_JSON)
                 .content(asJsonString(transferRequest)))
             	 .andExpect(status().isBadRequest())
             	 .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.name()))
             	 .andExpect(jsonPath("$.message").value("Validation failed for input data"))
             	.andExpect(jsonPath("$.validationErrors.payerAccNumber").value("Payer account number must be between 5 and 8 digits"))
         		.andExpect(jsonPath("$.validationErrors.payeeAccNumber").value("Payee account number must be between 5 and 8 digits"))
         		.andExpect(jsonPath("$.validationErrors.payeeBankCode").value("Payee bank code must be 6 characters"))
         		.andExpect(jsonPath("$.validationErrors.amount").value("Transfer amount must be greater than 0"))
         		.andExpect(jsonPath("$.validationErrors.currency").value("Currency must be one of the following:  EUR, GBP"));
     }
    
    @Test
    void testTransferGeneralException() throws Exception {
    	
    	TransferRequest transferRequest = new TransferRequest(
    			UUID.randomUUID(),
                "1234567",
                "987654",
                "Payee Bank",
                "BANK01",
                BigDecimal.valueOf(100.00),
                "GBP",
                Instant.now().toString()
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
   
    
    /**
     * Helper method to convert an object to a JSON string
     * @param obj
     * @return string 
     */
    private String asJsonString(final Object obj) {
        try {
            return objectMapper.writeValueAsString(obj); // Use the pre-configured objectMapper
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
