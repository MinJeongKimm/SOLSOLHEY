package com.solsolhey.finance.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "finance.api")
public class FinanceApiProperties {
    
    private String baseUrl = "https://finopenapi.ssafy.io/ssafy/api/v1/edu";
    private String apiKey;
    private int timeout = 10;
    private String institutionCode = "00100";
    private String fintechAppNo = "001";
}
