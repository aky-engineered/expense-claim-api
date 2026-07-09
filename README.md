# expense-claim-api

An API to manage employee expenses

## Prerequisites

- Java 17
- Maven 3.8+
- Docker Desktop

## How to Run

**1. Start the database**

```bash
docker-compose up -d
```

**2. Start the application**

```bash
./mvnw spring-boot:run
```

The application starts on http://localhost:8080
Swagger UI available at http://localhost:8080/swagger-ui.html

**To stop**

```bash
docker-compose down        # keeps data
docker-compose down -v     # wipes data (clean slate)
```
