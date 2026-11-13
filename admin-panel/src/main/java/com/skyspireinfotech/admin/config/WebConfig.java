package com.skyspireinfotech.admin.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path dir = Path.of(uploadDir).toAbsolutePath().normalize();
        try {
            // Path#toUri() only appends the trailing slash a directory location
            // needs if the directory already exists on disk, so create it
            // before computing the URI — otherwise the resource handler
            // silently 404s on every request.
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new IllegalStateException("Could not create upload directory: " + dir, e);
        }

        registry.addResourceHandler("/uploads/**").addResourceLocations(dir.toUri().toString());
    }
}
