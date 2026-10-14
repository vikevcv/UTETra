package com.ute.trasua.config;

import com.cloudinary.Cloudinary;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Cloudinary configuration bean for media asset upload and management.
 */
@Configuration
@Slf4j
public class CloudinaryConfig {

    @Value("${cloudinary.cloud-name:}")
    private String cloudName;

    @Value("${cloudinary.api-key:}")
    private String apiKey;

    @Value("${cloudinary.api-secret:}")
    private String apiSecret;

    @Bean
    public Cloudinary cloudinary() {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", cloudName != null ? cloudName.trim() : "");
        config.put("api_key", apiKey != null ? apiKey.trim() : "");
        config.put("api_secret", apiSecret != null ? apiSecret.trim() : "");
        config.put("secure", "true");

        if (cloudName == null || cloudName.isBlank() || apiKey == null || apiKey.isBlank()) {
            log.warn(">>> Cloudinary credentials are not fully set in .env. Initialized with empty/fallback config.");
        } else {
            log.info(">>> Cloudinary bean initialized successfully for cloud: [{}]", cloudName.trim());
        }

        return new Cloudinary(config);
    }
}
