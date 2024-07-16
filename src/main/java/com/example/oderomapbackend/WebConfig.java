package com.example.oderomapbackend;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // Allow CORS for all API endpoints
                .allowedOrigins("http://localhost:3000") // Replace with your frontend URL
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Specify allowed methods
                .allowCredentials(true);
    }
}