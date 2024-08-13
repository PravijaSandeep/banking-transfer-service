package com.exercise.banking.service.transfer.health;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.actuate.info.Info.Builder;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

@Component
public class CustomInfoContributor implements InfoContributor{

	@Override
	public void contribute(Builder builder) {
		Map<String, Object> details = new HashMap<>();
		details.put("app", System.getProperty("app.name", "Banking Transfer Service"));
        details.put("version", System.getProperty("app.version", "1.0.0"));
        details.put("description", System.getProperty("app.description", "API for transferring money between accounts"));

        builder.withDetail("application", details);
		
	}

}
