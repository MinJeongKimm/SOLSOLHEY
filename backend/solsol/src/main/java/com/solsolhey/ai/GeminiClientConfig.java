package com.solsolhey.ai;

import com.google.genai.Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import java.util.Optional;

@Configuration
public class GeminiClientConfig {

    private final Environment environment;

    public GeminiClientConfig(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public Client geminiClient() {
        // Only use GEMINI_API_KEY (no fallback) to avoid ambiguity
        String apiKey = environment.getProperty("GEMINI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                "Missing AI API key. Set GEMINI_API_KEY in backend/.env or OS env.");
        }

        // Build client with API key via public builder API
        return Client.builder()
                .apiKey(apiKey)
                .build();
    }
}
