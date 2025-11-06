package com.media.compression.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // ✅ Map /uploads/** to your local uploads folder
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/")   // maps to project root /uploads/
                .setCachePeriod(3600); // optional caching
    }
}
