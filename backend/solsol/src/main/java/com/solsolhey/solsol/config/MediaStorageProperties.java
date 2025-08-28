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
}
