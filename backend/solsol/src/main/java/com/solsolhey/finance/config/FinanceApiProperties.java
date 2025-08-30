package com.solsolhey.finance.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "finance.api")
public class FinanceApiProperties {
    
    private String baseUrl = "https://finopenapi.ssafy.io/ssafy/api/v1/edu";
    /**
     * 멤버 관련 엔드포인트의 루트 URL (예: https://finopenapi.ssafy.io/ssafy/api/v1)
     */
    private String memberBaseUrl = "https://finopenapi.ssafy.io/ssafy/api/v1";
    private String apiKey;
    private int timeout = 10;
    private String institutionCode = "00100";
    private String fintechAppNo = "001";
}
