package com.pleasebookme.server.security.authorization.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;

@Configuration
public class AuthorizationExpressionHandlerConfig {

    @Bean
    static MethodSecurityExpressionHandler methodSecurityExpressionHandler(
        PermissionEvaluator permissionEvaluator
    ) {
        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();

        handler.setPermissionEvaluator(permissionEvaluator);

        return handler;
    }
}
