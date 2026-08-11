package com.pleasebookme.server.security.config;

import com.pleasebookme.server.security.config.property.CorsProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@EnableConfigurationProperties(CorsProperties.class)
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    private final CorsProperties corsProperties;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins(corsProperties.allowedOrigins().toArray(String[]::new))
            .allowedMethods(corsProperties.allowedMethods().toArray(String[]::new))
            .allowedHeaders(corsProperties.allowedHeaders().toArray(String[]::new))
            .exposedHeaders(exposedHeaders().toArray(String[]::new))
            .allowCredentials(corsProperties.allowCredentials())
            .maxAge(corsProperties.maxAge().toSeconds());
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(corsProperties.allowedOrigins());
        configuration.setAllowedMethods(corsProperties.allowedMethods());
        configuration.setAllowedHeaders(corsProperties.allowedHeaders());
        configuration.setExposedHeaders(exposedHeaders());
        configuration.setAllowCredentials(corsProperties.allowCredentials());
        configuration.setMaxAge(corsProperties.maxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);

        return source;
    }

    private List<String> exposedHeaders() {
        return corsProperties.exposedHeaders() != null
            ? corsProperties.exposedHeaders()
            : List.of();
    }
}
