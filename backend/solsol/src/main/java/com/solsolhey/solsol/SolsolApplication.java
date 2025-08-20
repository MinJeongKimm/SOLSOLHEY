package com.solsolhey.solsol;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SolsolApplication {

	public static void main(String[] args) {
		SpringApplication.run(SolsolApplication.class, args);
	}

}
