package com.exercise.banking.service.transfer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.EnumMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.exercise.banking.service.transfer.model.TransferType;
import com.exercise.banking.service.transfer.service.impl.InterBankTransferService;
import com.exercise.banking.service.transfer.service.impl.IntraBankTransferService;

class TransferServiceSelectorTest {

	@Mock
	private InterBankTransferService interBankSvc;

	@Mock
	private IntraBankTransferService intraBankSvc;

	private TransferServiceSelector transferServiceSelector;

	@BeforeEach
	void setUp() {
		// Initialize mocks
		MockitoAnnotations.openMocks(this);

		// Create the EnumMap with the mocked services
		Map<TransferType, TransferService> services = new EnumMap<>(TransferType.class);
		services.put(TransferType.INTER_BANK_TRANSFER, interBankSvc);
		services.put(TransferType.INTRA_BANK_TRANSFER, intraBankSvc);

		// Create the TransferServiceSelector instance
		transferServiceSelector = new TransferServiceSelector(services);
	}


	@Test
	void testGetService_IntraBank() {
		// Given
		String payerBankCode = "A0001";
		String payeeBankCode = "A0001";

		TransferService service = transferServiceSelector.getService(payerBankCode, payeeBankCode);
		assertEquals(intraBankSvc, service);
	}

	@Test
	void testGetService_InterBank() {
		String payerBankCode = "A0001";
		String payeeBankCode = "B0001";

		TransferService service = transferServiceSelector.getService(payerBankCode, payeeBankCode);
		assertEquals(interBankSvc, service);
	}

}
