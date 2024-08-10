package com.exercise.banking.service.transfer.money.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exercise.banking.service.transfer.money.config.BankConfiguration;
import com.exercise.banking.service.transfer.money.dto.TransferRequest;
import com.exercise.banking.service.transfer.money.dto.TransferResponse;
import com.exercise.banking.service.transfer.money.service.TransferService;
import com.exercise.banking.service.transfer.money.service.TransferServiceSelector;

import jakarta.validation.Valid;

@RestController
@Validated
@RequestMapping("/api/transfers")
public class TransferController {
	
	private static final Logger logger = LoggerFactory.getLogger(TransferController.class);

    private final TransferServiceSelector transferServiceSelector;
	
    private final BankConfiguration config;
	
	public TransferController(TransferServiceSelector transferServiceSelector, BankConfiguration config) {
        this.transferServiceSelector = transferServiceSelector;
        this.config = config;
    }
	
	@PostMapping
	public ResponseEntity<TransferResponse> transfer(@Valid @RequestBody TransferRequest request) {
	    logger.info("Received Transfer Request : {}", request);
	    
	    TransferService service = transferServiceSelector.getService(config.getBankCode(), request.getPayeeBankCode());
	    logger.debug("Selected transfer service: {}", service.getClass().getSimpleName());
	    
	    TransferResponse response = service.performTransfer(request);
	    return ResponseEntity.ok(response);
	}

}
