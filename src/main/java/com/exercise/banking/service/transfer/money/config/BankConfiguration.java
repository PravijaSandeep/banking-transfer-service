package com.exercise.banking.service.transfer.money.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BankConfiguration {
	
	@Value("${transfer.source.bank.name:Natwest}")
    private String bankName;

    public String getBankName() {
        return bankName;
    }
    
    @Value("${transfer.source.bank.codee:nw123}")
    private String bankCode;

    public String getBankCode() {
        return bankCode;
    }

}
