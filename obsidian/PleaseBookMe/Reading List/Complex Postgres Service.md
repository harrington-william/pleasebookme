version: '3.8'
```yaml
services:
  rabbitmq:
    image: rabbitmq:4.0-management-alpine
    container_name: production_rabbitmq
    restart: always
    # Apply resource constraints to prevent container crash or host exhaustion
    deploy:
      resources:
        limits:
          cpus: '2.0'
          memory: 2gb
        reservations:
          memory: 1gb
    # Port mappings: Avoid exposing ports publicly if services are in the same network
    ports:
      - "127.0.0.1:5672:5672"   # AMQP port (bound to localhost for safety)
      - "127.0.0.1:15672:15672" # Management UI (bound to localhost)
    environment:
      # Inject credentials via variables or a hidden .env file instead of hardcoding
      RABBITMQ_DEFAULT_USER: ${RABBITMQ_ADMIN_USER:-admin}
      RABBITMQ_DEFAULT_PASS: ${RABBITMQ_ADMIN_PASSWORD}
    volumes:
      # Explicit definition for persisting both definitions and state data
      - rabbitmq_data:/var/lib/rabbitmq
    networks:
      - backend_network
    healthcheck:
      test: ["CMD", "rabbitmq-diagnostics", "-q", "ping"]
      interval: 10s
      timeout: 5s
      retries: 3

volumes:
  rabbitmq_data:
    driver: local

networks:
  backend_network:
    driver: bridge
```
