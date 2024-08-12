package com.exercise.banking.service.transfer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BankConfiguration {
	
	@Value("${transfer.source.bank.name:BANK_A}")
    private String bankName;

    public String getBankName() {
        return bankName;
    }
    
    @Value("${transfer.source.bank.code:A00001}")
    private String bankCode;

    public String getBankCode() {
        return bankCode;
    }

}
