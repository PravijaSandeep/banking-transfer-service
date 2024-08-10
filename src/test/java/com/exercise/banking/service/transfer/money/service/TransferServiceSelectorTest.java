package com.exercise.banking.service.transfer.money.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.exercise.banking.service.transfer.money.service.impl.InterBankTransferService;
import com.exercise.banking.service.transfer.money.service.impl.IntraBankTransferService;

class TransferServiceSelectorTest {
	
	@Mock
	InterBankTransferService interBankSvc;
	
	@Mock
	IntraBankTransferService intraBankSvc;
	
	@Test
	void testServiceSelection() {
		
		TransferServiceSelector selector = new TransferServiceSelector(interBankSvc,intraBankSvc);
		TransferService svc = selector.getService("test", "test");
		assertEquals(svc,intraBankSvc);
		
		svc = selector.getService("test", "test1");
		assertEquals(svc,interBankSvc);
		
		assertThrows(IllegalArgumentException.class,() -> selector.getService("test", null));
		
	}

}
