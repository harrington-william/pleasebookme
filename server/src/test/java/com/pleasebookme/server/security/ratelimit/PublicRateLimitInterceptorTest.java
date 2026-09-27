package com.pleasebookme.server.security.ratelimit;

import com.pleasebookme.server.security.ratelimit.engine.RateLimiter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Duration;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PublicRateLimitInterceptorTest {
    @Mock private RateLimiter rateLimiter;

    private PublicRateLimitInterceptor interceptor;

    @BeforeEach
    void setUp() {
        RateLimitProperties properties = new RateLimitProperties(
            new RateLimitProperties.Public(
                new RateLimitProperties.Limit(120, Duration.ofMinutes(1)),
                new RateLimitProperties.Limit(5, Duration.ofMinutes(1))
            )
        );
        interceptor = new PublicRateLimitInterceptor(rateLimiter, properties);
    }

    @Test
    void options_bypassesLimiter() {
        MockHttpServletRequest request = request("OPTIONS");

        interceptor.preHandle(request, new MockHttpServletResponse(), new Object());

        verify(rateLimiter, never()).check(
            org.mockito.ArgumentMatchers.anyString(),
            org.mockito.ArgumentMatchers.anyString(),
            org.mockito.ArgumentMatchers.anyInt(),
            org.mockito.ArgumentMatchers.any(Duration.class)
        );
    }

    @Test
    void get_usesReadBucketAndRemoteAddress() {
        interceptor.preHandle(request("GET"), new MockHttpServletResponse(), new Object());

        verify(rateLimiter).check("read", "1.2.3.4", 120, Duration.ofMinutes(1));
    }

    @Test
    void post_usesWriteBucketAndRemoteAddress() {
        interceptor.preHandle(request("POST"), new MockHttpServletResponse(), new Object());

        verify(rateLimiter).check("write", "1.2.3.4", 5, Duration.ofMinutes(1));
    }

    private MockHttpServletRequest request(String method) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, "/api/v1/public/acme");
        request.setRemoteAddr("1.2.3.4");
        return request;
    }
}
