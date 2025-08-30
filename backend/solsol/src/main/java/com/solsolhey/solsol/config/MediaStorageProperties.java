package com.solsolhey.solsol.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "media")
@Getter
@Setter
public class MediaStorageProperties {
    private String uploadDir = "./uploads";
    private int snapshotMaxEntries = 20;
    // 공개 접근용 베이스 URL (예: https://backend-production-xxxx.up.railway.app)
    // 비어있으면 상대 경로(/uploads/...)를 반환합니다.
    private String publicBaseUrl = "";
}
