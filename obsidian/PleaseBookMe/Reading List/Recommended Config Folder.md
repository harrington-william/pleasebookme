In a large Spring Boot application, treating `SecurityConfig.java` as the single configuration class is generally considered an anti-pattern. As the project grows, configuration should be split by **concern** rather than by framework.

A mature Spring Boot project often contains dozens of configuration classes.

---

# Typical Configuration Structure

A common organization looks like this:

```bash
src/main/java/com/example/project
│
├── config/
│   ├── security/
│   ├── database/
│   ├── cache/
│   ├── messaging/
│   ├── web/
│   ├── serialization/
│   ├── scheduling/
│   ├── async/
│   ├── monitoring/
│   ├── storage/
│   ├── openapi/
│   └── properties/
```

Each package contains one responsibility.

---

# 1. Security Configuration

Instead of

```
SecurityConfig.java
```

split it.

```bash
config/
└── security/
    ├── SecurityConfig.java
    ├── JwtAuthenticationFilter.java
    ├── JwtAuthenticationProvider.java
    ├── JwtProperties.java
    ├── CorsConfig.java
    ├── PasswordEncoderConfig.java
    ├── OAuth2Config.java
    ├── MethodSecurityConfig.java
    └── SecurityExceptionHandler.java
```

Responsibilities

- Security filter chain
    
- JWT
    
- OAuth2
    
- PasswordEncoder
    
- AuthenticationProvider
    
- CORS
    
- Authorization rules
    
- Exception handling
    

Each should be independent.

---

# 2. Database Configuration

Large applications usually have much more than

```properties
spring.datasource...
```

Example

```bash
config/database/
    ├── DataSourceConfig.java
    ├── JpaConfig.java
    ├── TransactionConfig.java
    ├── FlywayConfig.java
    ├── MyBatisConfig.java
    ├── HibernateConfig.java
    └── AuditConfig.java
```

Possible responsibilities

- multiple datasource
    
- connection pool
    
- Hibernate tuning
    
- naming strategy
    
- audit
    
- transactions
    

---

# 3. Web Configuration

Spring MVC configuration.

```bash
config/web/
    ├── WebMvcConfig.java
    ├── CorsConfig.java
    ├── LocaleConfig.java
    ├── JacksonConfig.java
    ├── InterceptorConfig.java
    ├── FormatterConfig.java
    └── StaticResourceConfig.java
```

Example

```java
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(...) {

    }

    @Override
    public void addResourceHandlers(...) {

    }
}
```

---

# 4. Jackson Configuration

JSON serialization deserves its own config.

```java
JacksonConfig.java
```

Example

```java
@Bean
ObjectMapper objectMapper() {

    return JsonMapper.builder()
            .findAndAddModules()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();
}
```

---

# 5. Cache Configuration

```bash
config/cache/
    CacheConfig.java
```

Example

```java
@EnableCaching
@Configuration
public class CacheConfig {

}
```

Configure

- Redis
    
- Caffeine
    
- EhCache
    
- Hazelcast
    

---

# 6. Redis Configuration

Sometimes cache and Redis are separated.

```java
RedisConfig.java
```

Contains

- RedisConnectionFactory
    
- RedisTemplate
    
- serializers
    
- pub/sub
    

---

# 7. Scheduling Configuration

```java
config/scheduling/
    SchedulerConfig.java
```

```java
@EnableScheduling
```

Custom

- ThreadPoolTaskScheduler
    
- Cron jobs
    
- Quartz
    

---

# 8. Async Configuration

```java
config/async/
    AsyncConfig.java
```

Example

```java
@EnableAsync
```

```java
@Bean
Executor taskExecutor() {

}
```

Used by

```java
@Async
```

---

# 9. OpenAPI / Swagger

```java
config/openapi/
    OpenApiConfig.java
```

Contains

- JWT support
    
- API info
    
- contact
    
- servers
    
- groups
    

---

# 10. Messaging Configuration

If using Kafka

```java
KafkaConfig.java
```

If RabbitMQ

```java
RabbitMQConfig.java
```

If ActiveMQ

```java
ActiveMQConfig.java
```

Contains

- producer
    
- consumer
    
- serializers
    
- retry
    
- dead letter queue
    

---

# 11. Storage Configuration

Cloud storage.

```java
StorageConfig.java
```

Examples

- AWS S3
    
- Azure Blob
    
- MinIO
    
- Google Cloud Storage
    

---

# 12. Email Configuration

```java
MailConfig.java
```

Contains

- JavaMailSender
    
- SMTP
    
- templates
    

---

# 13. Monitoring Configuration

```java
MonitoringConfig.java
```

Examples

- Micrometer
    
- Prometheus
    
- OpenTelemetry
    
- Zipkin
    

---

# 14. Validation Configuration

```java
ValidationConfig.java
```

Example

```java
@Bean
Validator validator() {

}
```

---

# 15. Bean Configuration

Sometimes we need third-party beans.

```java
BeanConfig.java
```

Example

```java
@Bean
ModelMapper mapper() {

}

@Bean
Clock clock() {

}

@Bean
BCryptPasswordEncoder encoder() {

}
```

---

# 16. Properties Configuration

Instead of using many

```java
@Value
```

use

```java
@ConfigurationProperties
```

```
config/properties/
    JwtProperties.java
    AwsProperties.java
    MailProperties.java
    RedisProperties.java
```

Example

```java
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String secret;
    private long expiration;
}
```

Cleaner than

```java
@Value("${jwt.secret}")
```

---

# 17. Feature Configuration

Sometimes features need their own configuration.

```bash
config/feature/
    SearchConfig.java
    PaymentConfig.java
    RecommendationConfig.java
```

Example

```java
@Configuration
public class PaymentConfig {

    @Bean
    StripeClient stripeClient() {

    }

}
```

---

# 18. Profile-Specific Configuration

```bash
application.yml

application-dev.yml

application-test.yml

application-prod.yml

application-local.yml
```

Combined with

```java
@Profile("dev")
```

Example

```java
@Configuration
@Profile("dev")
public class MockMailConfig {

}
```

---

# Example Layout for a Large Enterprise Project

```text
config/
│
├── security/
│   ├── SecurityConfig.java
│   ├── JwtConfig.java
│   ├── CorsConfig.java
│   ├── OAuth2Config.java
│   └── MethodSecurityConfig.java
│
├── database/
│   ├── DataSourceConfig.java
│   ├── JpaConfig.java
│   ├── FlywayConfig.java
│   └── TransactionConfig.java
│
├── cache/
│   ├── CacheConfig.java
│   └── RedisConfig.java
│
├── messaging/
│   ├── KafkaConfig.java
│   └── RabbitConfig.java
│
├── web/
│   ├── WebMvcConfig.java
│   ├── JacksonConfig.java
│   ├── LocaleConfig.java
│   └── InterceptorConfig.java
│
├── async/
│   └── AsyncConfig.java
│
├── scheduler/
│   └── SchedulerConfig.java
│
├── monitoring/
│   └── MonitoringConfig.java
│
├── openapi/
│   └── OpenApiConfig.java
│
├── storage/
│   └── StorageConfig.java
│
└── properties/
    ├── JwtProperties.java
    ├── AwsProperties.java
    ├── MailProperties.java
    └── RedisProperties.java
```

---

# Design Principle

As a rule of thumb:

- 🎯 **One configuration class = one infrastructure concern.**
    
- Keep configuration packages independent and cohesive.
    
- Prefer `@ConfigurationProperties` over scattered `@Value` injections.
    
- Avoid a "God" `SecurityConfig` or `AppConfig` that configures unrelated infrastructure.
    
- Group configurations by **feature or infrastructure domain**, not by annotation type.
    

For enterprise applications with dozens of modules, it's also common to split configurations into module-specific packages (e.g., `auth/config`, `payment/config`, `notification/config`) so that each module owns its own infrastructure wiring instead of relying on a single global `config` package. This aligns well with modular monoliths and simplifies extracting services later if needed.