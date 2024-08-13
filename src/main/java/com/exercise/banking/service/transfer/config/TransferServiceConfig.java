package com.exercise.banking.service.transfer.config;

import java.util.EnumMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.exercise.banking.service.transfer.model.TransferType;
import com.exercise.banking.service.transfer.service.TransferService;
import com.exercise.banking.service.transfer.service.TransferServiceSelector;
import com.exercise.banking.service.transfer.service.impl.InterBankTransferService;
import com.exercise.banking.service.transfer.service.impl.IntraBankTransferService;

@Configuration
public class TransferServiceConfig {

    @Bean
    Map<TransferType, TransferService> transferServices(InterBankTransferService interBankSvc, 
                                                         IntraBankTransferService intraBankSvc) {
    	
        Map<TransferType, TransferService> services = new EnumMap<>(TransferType.class);
        services.put(TransferType.INTER_BANK_TRANSFER, interBankSvc);
        services.put(TransferType.INTRA_BANK_TRANSFER, intraBankSvc);
        return services;
    }

    @Bean
    TransferServiceSelector transferServiceSelector(Map<TransferType, TransferService> services) {
        return new TransferServiceSelector(services);
    }
}
