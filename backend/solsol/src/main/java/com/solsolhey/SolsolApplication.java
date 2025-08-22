package com.solsolhey;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.solsolhey")
@EntityScan(basePackages = "com.solsolhey")
@EnableJpaRepositories(basePackages = "com.solsolhey")
@EnableScheduling
public class SolsolApplication {

	public static void main(String[] args) {
		SpringApplication.run(SolsolApplication.class, args);
	}

}
