package com.example.Boggle.Security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfigurer implements WebMvcConfigurer {

    /**
     * addCorsMappings: Adds frontend to list of hosts
     * allowed to make requests to the backend
     *
     * @param registry CORS registry to append to
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Configure CORS repository; allow all api endpoints
        registry.addMapping("/**")
                // Allow frontend port
                .allowedOrigins("http://localhost:5173")
                // Allow get, post, and put requests
                .allowedMethods("GET", "POST", "PUT","OPTIONS")
                // Allow all headers
                .allowedHeaders("*");
    }
}

