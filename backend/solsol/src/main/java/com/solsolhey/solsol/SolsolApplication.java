package com.solsolhey.solsol;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SolsolApplication {

	public static void main(String[] args) {
		// .env 파일 경로 설정 (상위 디렉토리의 .env 파일 사용)
		System.setProperty("dotenv.path", "../.env");
		
		SpringApplication.run(SolsolApplication.class, args);
	}

}
