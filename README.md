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

**To stop**

```bash
docker-compose down        # keeps data
docker-compose down -v     # wipes data
```

## Swagger Docs

The swagger docs provide a comprehensive view of all endpoints, schemas and responses.
This swagger implementation also contains authentication, so the application can be fully tested through swagger.

Swagger UI is available at: http://localhost:8080/swagger-ui.html

To test authenticated endpoints:

1. Call POST /api/auth/login with valid credentials (see below for credentials required)
2. Copy the token from the response
3. Click "Authorize" and paste the token
4. All protected endpoints are now accessible

## Credentials (task purposes only)

| username      |    password    |         role |
|:--------------|:--------------:|-------------:|
| john.smith    |  Password123!  | **EMPLOYEE** |
| jane.doe      |  Password456!  | **EMPLOYEE** |
| mike.approver | ApproverPass1! | **APPROVER** |

## Endpoints and Authorization

Here is a breakdown of the API endpoints and the corresponding authorization via role

| Operation |        Endpoint path        | Authorization? |                                   Notes |
|:----------|:---------------------------:|---------------:|----------------------------------------:|
| **POST**  |       /api/auth/login       |                |                    returns JWT (public) |
| **POST**  |         /api/claims         |   **EMPLOYEE** |                  EMPLOYEE submits claim |
| **GET**   |         /api/claims         |   **EMPLOYEE** | EMPLOYEE can view thier own claims only |
| **GET**   |      /api/claims/{id}       |   **EMPLOYEE** |       EMPLOYEE can view thier own claim |
| **GET**   |   /api/approvals/pending    |   **APPROVER** |    APPROVER can view all pending claims |
| **POST**  | /api/approvals/{id}/approve |   **APPROVER** |               APPROVER approves a claim |
| **POST**  | /api/approvals/{id}/reject  |   **APPROVER** |                APPROVER rejects a claim |
| **GET**   |   /api/claims/{id}/audit    |   **APPROVER** |        APPROVER retrieves audit history |
