package com.solsolhey.ai;

import com.google.genai.Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class GeminiClientConfig {

    private final Environment environment;

    public GeminiClientConfig(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public Client geminiClient() {
        // Prefer GEMINI_API_KEY; fall back to GOOGLE_API_KEY (google-genai default)
        String apiKey = environment.getProperty("GEMINI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            apiKey = environment.getProperty("GOOGLE_API_KEY");
        }

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                "Missing AI API key. Set GEMINI_API_KEY or GOOGLE_API_KEY in backend/.env or OS env.");
        }

        // Provide key directly to the client instead of relying on env var name.
        return new Client(apiKey);
    }
}
