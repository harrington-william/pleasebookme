package com.pleasebookme.server.security.ratelimit;

import com.pleasebookme.server.security.ratelimit.engine.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class PublicRateLimitInterceptor implements HandlerInterceptor {
    private final RateLimiter rateLimiter;
    private final RateLimitProperties properties;

    @Override
    public boolean preHandle(
        HttpServletRequest request,
        HttpServletResponse response,
        Object handler
    ) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        boolean isWrite = "POST".equalsIgnoreCase(request.getMethod());
        String bucket = isWrite ? "write" : "read";
        RateLimitProperties.Limit limit = isWrite
            ? properties.publicLimits().write()
            : properties.publicLimits().read();

        rateLimiter.check(
            bucket,
            request.getRemoteAddr(),
            limit.limit(),
            limit.window()
        );
        return true;
    }
}
