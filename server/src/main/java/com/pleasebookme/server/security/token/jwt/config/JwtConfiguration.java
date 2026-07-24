package com.pleasebookme.server.security.token.jwt.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

// Register configuration
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfiguration {
}
