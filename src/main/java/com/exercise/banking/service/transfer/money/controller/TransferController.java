package com.exercise.banking.service.transfer.money.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exercise.banking.service.transfer.money.config.BankConfiguration;
import com.exercise.banking.service.transfer.money.dto.ErrorResponse;
import com.exercise.banking.service.transfer.money.dto.TransferRequest;
import com.exercise.banking.service.transfer.money.dto.TransferResponse;
import com.exercise.banking.service.transfer.money.service.TransferService;
import com.exercise.banking.service.transfer.money.service.TransferServiceSelector;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@Validated
@RequestMapping(value = "/api/transfers", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Transfer Service", description = "Money transfer related operations")
public class TransferController {

    private static final Logger logger = LoggerFactory.getLogger(TransferController.class);

    private final TransferServiceSelector transferServiceSelector;
    private final BankConfiguration config;

    public TransferController(TransferServiceSelector transferServiceSelector, BankConfiguration config) {
        this.transferServiceSelector = transferServiceSelector;
        this.config = config;
    }

    @PostMapping
    @Operation(
        summary = "Transfer money between accounts", 
        description = "Performs a money transfer between payer and payee accounts",
        responses = {
            @ApiResponse(
                responseCode = "200", 
                description = "Transfer Successful", 
                content = @Content(schema = @Schema(implementation = TransferResponse.class))
            ),
            @ApiResponse(
                responseCode = "400", 
                description = "Invalid input or Insufficient funds", 
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                responseCode = "404", 
                description = "Account Not Found", 
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                responseCode = "500", 
                description = "Internal Server Error - An unexpected error occurred", 
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
        }
    )
    public ResponseEntity<TransferResponse> transfer(@Valid @RequestBody TransferRequest request, HttpServletRequest httpServletRequest) {
    	
    	MDC.put("requestId", request.getRequestId().toString()); // add requestId to MDC, so that it is available in all current request log lines.
    	try {

    		logger.info("Received Transfer Request");

    		TransferService service = transferServiceSelector.getService(config.getBankCode(), request.getPayeeBankCode());

    		TransferResponse response = service.performTransfer(request);
    		return ResponseEntity.ok(response);
    	}finally {
    		// Ensure to clear the MDC after the request is processed
            MDC.remove("requestId");
    	}
    }
}
