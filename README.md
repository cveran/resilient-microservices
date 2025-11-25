# Resilient Microservices

A collection of backend microservices focused on resilience and stability patterns. Each service showcases techniques like timeouts, graceful degradation, caching, error handling, and performance tuning using modern Java (Java 21), Spring Boot, and RestClient.

## Overview

This repository provides practical, production-oriented examples of how to design resilient, performant APIs in a high-traffic environment.

### Technologies

- **Java 21** - Latest LTS version with modern language features
- **Spring Boot 3.3** - Production-ready framework
- **RestClient** - Modern HTTP client (replacement for RestTemplate)
- **Resilience4j** - Fault tolerance library
- **Caffeine** - High-performance caching
- **Spring Cache** - Declarative caching abstraction

## Resilience Patterns Demonstrated

### 1. Timeout Handling
- Configurable connection and read timeouts via RestClient
- TimeLimiter for async operations with time bounds
- Graceful timeout handling with fallback responses

### 2. Circuit Breaker
- Prevents cascading failures when external services are down
- Automatic recovery with half-open state
- Configurable failure rate thresholds

### 3. Retry Mechanism
- Automatic retry for transient failures
- Exponential backoff strategy
- Configurable retry attempts and intervals

### 4. Caching
- High-performance caching with Caffeine
- Cache eviction strategies
- Cache statistics for monitoring

### 5. Graceful Degradation
- Fallback responses when services fail
- Degraded but functional responses
- Clear indication of service status

### 6. Error Handling
- Global exception handler
- Consistent API response format
- Proper HTTP status codes

## Project Structure

```
resilience-service/
├── src/main/java/com/example/resilience/
│   ├── ResilienceServiceApplication.java  # Main application
│   ├── config/
│   │   ├── RestClientConfig.java          # RestClient with timeouts
│   │   └── CacheConfig.java               # Caffeine cache configuration
│   ├── controller/
│   │   ├── ProductController.java         # Product REST endpoints
│   │   └── UserController.java            # User REST endpoints
│   ├── service/
│   │   ├── ProductService.java            # Product business logic
│   │   └── UserService.java               # User business logic
│   ├── model/
│   │   ├── Product.java                   # Product model
│   │   ├── User.java                      # User model
│   │   └── ApiResponse.java               # Generic API response
│   └── exception/
│       ├── GlobalExceptionHandler.java    # Global error handling
│       ├── ServiceUnavailableException.java
│       └── ResourceNotFoundException.java
└── src/main/resources/
    └── application.yml                    # Application configuration
```

## Getting Started

### Prerequisites

- Java 21 or higher
- Gradle 8.7 or higher

### Building the Project

```bash
cd resilience-service
./gradlew build
```

### Running the Application

```bash
./gradlew bootRun
```

The application starts on `http://localhost:8080`

### Running Tests

```bash
./gradlew test
```

## API Endpoints

### Products API

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/products` | Get all products (cached) |
| GET | `/api/v1/products/{id}` | Get product by ID (cached, circuit breaker) |
| GET | `/api/v1/products/{id}/async` | Get product async (timeout handling) |

### Users API

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/users` | Get all users (cached) |
| GET | `/api/v1/users/{id}` | Get user by ID (cached, circuit breaker) |
| DELETE | `/api/v1/users/{id}/cache` | Evict cache for specific user |
| DELETE | `/api/v1/users/cache` | Evict all user caches |

### Health & Metrics

| Endpoint | Description |
|----------|-------------|
| `/actuator/health` | Application health status |
| `/actuator/metrics` | Application metrics |
| `/actuator/caches` | Cache statistics |

## Configuration

### Timeout Configuration

```yaml
resilience:
  http:
    connect-timeout: 5000   # 5 seconds
    read-timeout: 10000     # 10 seconds
```

### Circuit Breaker Configuration

```yaml
resilience4j:
  circuitbreaker:
    instances:
      productService:
        sliding-window-size: 10
        failure-rate-threshold: 50
        wait-duration-in-open-state: 30s
```

### Cache Configuration

```yaml
# Programmatic configuration in CacheConfig.java
# - Initial capacity: 100 entries
# - Maximum size: 500 entries  
# - TTL: 10 minutes
```

## API Response Format

All endpoints return responses in a consistent format:

```json
{
  "data": { ... },
  "status": "SUCCESS",
  "message": null,
  "timestamp": "2024-01-15T10:30:00Z",
  "fromCache": false,
  "fallback": false
}
```

### Response Status Values

- `SUCCESS` - Request completed successfully
- `DEGRADED` - Service returned fallback response
- `ERROR` - Request failed

## Resilience Scenarios

### Circuit Breaker States

1. **CLOSED** - Normal operation, requests pass through
2. **OPEN** - Too many failures, requests fail fast with fallback
3. **HALF_OPEN** - Testing if service recovered

### Fallback Behavior

When external services fail:
- Products: Returns `Product.fallback(id)` with "Unavailable" name
- Users: Returns `User.fallback(id)` with "unknown" username

## Monitoring

The application exposes actuator endpoints for monitoring:

```bash
# Health check
curl http://localhost:8080/actuator/health

# Circuit breaker status
curl http://localhost:8080/actuator/health | jq '.components.circuitBreakers'

# Cache statistics
curl http://localhost:8080/actuator/caches
```

## Best Practices Demonstrated

1. **Fail Fast**: Circuit breaker prevents waiting for slow services
2. **Graceful Degradation**: Return cached or default data when possible
3. **Timeout Everything**: Never wait indefinitely for external calls
4. **Retry with Backoff**: Handle transient failures automatically
5. **Monitor Everything**: Use actuator for observability
6. **Consistent Error Handling**: Global exception handler for uniform responses

## License

This project is provided as educational material for demonstrating resilience patterns in microservices.