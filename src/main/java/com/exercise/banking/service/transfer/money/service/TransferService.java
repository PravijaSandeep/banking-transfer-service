package com.exercise.banking.service.transfer.money.service;

import com.exercise.banking.service.transfer.money.dto.TransferRequest;
import com.exercise.banking.service.transfer.money.dto.TransferResponse;


public interface TransferService {
	TransferResponse performTransfer(TransferRequest transferRequest);
}
