package com.exercise.banking.service.transfer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;


@SpringJUnitConfig
@ContextConfiguration(classes = MoneyTransferApplication.class)
class MoneyTransferApplicationTests {
	
	@Autowired
    private ApplicationContext applicationContext;

	@Test
	void contextLoads() {
		boolean intraSvcbeanExists = applicationContext.containsBean("intraBankTransferService");
		boolean interSvcbeanExists = applicationContext.containsBean("intraBankTransferService");

        assertThat(intraSvcbeanExists && interSvcbeanExists).isTrue();
	}

}
