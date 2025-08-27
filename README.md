# Business Rule Engine - Spring Boot REST API

A comprehensive Spring Boot Business Rule Engine service that accepts typeOfRequest + raw JSON payload, loads JSON configurations, validates input, runs SpEL-based transformations, supports rule chaining, and calls external APIs using RestTemplate.

## Features

- **Configuration-driven rules**: Each request type has a JSON configuration file
- **SpEL-based transformations**: Powerful expression language for data manipulation
- **Validation engine**: Declarative validation with required fields, types, regex, and expression conditions
- **External API integration**: RestTemplate-based calls with template processing
- **Rule chaining**: Sequential rule execution with conditional branching
- **Execution tracing**: Detailed trace of all rule executions and transformations
- **Security**: API key validation via X-API-KEY header
- **Observability**: Structured logging, health checks, and actuator endpoints
- **Docker support**: Complete containerization with docker-compose

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher

## Building the Application

```bash
mvn clean compile
```

## Running the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## API Endpoints

### 1. Rule Processing
- **POST** `/rules/consume`
- **Headers**: `X-API-KEY: change-me` (required)
- **Body**:
```json
{
  "typeOfRequest": "customer_onboard",
  "payload": {
    "firstName": "   Aisha ",
    "lastName": " Khan ",
    "email": "aisha.khan@example.corp",
    "age": 32,
    "country": "AE"
  }
}
```

### 2. Health Check
- **GET** `/rules/health`
- Returns application status

### 3. Configuration Reload
- **POST** `/rules/config/reload/{type}`
- **Headers**: `X-API-KEY: change-me` (required)
- Reloads configuration for a specific request type

### 4. Actuator Endpoints
- **GET** `/actuator/health` - Detailed health information
- **GET** `/actuator/info` - Application information

## Testing with curl

### Rule Processing
```bash
curl -X POST http://localhost:8080/rules/consume \
  -H "Content-Type: application/json" \
  -H "X-API-KEY: change-me" \
  -d '{
    "typeOfRequest": "customer_onboard",
    "payload": {
      "firstName": "   Aisha ",
      "lastName": " Khan ",
      "email": "aisha.khan@example.corp",
      "age": 32,
      "country": "AE"
    }
  }'
```

### Health Check
```bash
curl -X GET http://localhost:8080/rules/health
```

### Configuration Reload
```bash
curl -X POST http://localhost:8080/rules/config/reload/customer_onboard \
  -H "X-API-KEY: change-me"
```

### Actuator Health
```bash
curl -X GET http://localhost:8080/actuator/health
```

### Test with Mock Services
```bash
# Start mock services
docker-compose up mock-kyc mock-i18n -d

# Test with external API calls
curl -X POST http://localhost:8080/rules/consume \
  -H "Content-Type: application/json" \
  -H "X-API-KEY: change-me" \
  -d '{
    "typeOfRequest": "customer_onboard",
    "payload": {
      "firstName": "John",
      "lastName": "Doe",
      "email": "john.doe@example.com",
      "age": 25,
      "country": "US"
    }
  }'
```

## Response Format

The rule engine returns a structured response with execution details:

```json
{
  "requestId": "uuid",
  "typeOfRequest": "customer_onboard",
  "valid": true,
  "errors": [],
  "transformedPayload": {
    "firstName": "Aisha",
    "lastName": "Khan",
    "email": "aisha.khan@example.corp",
    "age": 32,
    "country": "AE",
    "fullName": "Aisha Khan",
    "riskScore": 5,
    "kyc": {
      "status": "APPROVED",
      "score": 85,
      "riskLevel": "LOW"
    },
    "messages": {
      "allocateApproversMessage": "Please allocate the approvers for this request"
    },
    "processedAt": "2024-01-01T12:00:00"
  },
  "trace": [
    {
      "ruleId": "v1-requireds",
      "description": "Basic required fields",
      "timestamp": "2024-01-01T12:00:00",
      "status": "SUCCESS"
    },
    {
      "ruleId": "t1-normalize-names",
      "description": "Trim names and build fullName",
      "timestamp": "2024-01-01T12:00:00",
      "status": "TRANSFORMED"
    }
  ],
  "externalCalls": [
    {
      "ruleId": "x1-kyc-check",
      "url": "https://example-kyc.local/kyc/check",
      "method": "POST",
      "timestamp": "2024-01-01T12:00:00",
      "result": {
        "status": "APPROVED",
        "score": 85
      }
    }
  ]
}
```

## Rule Configuration

Rules are defined in JSON files under `src/main/resources/rules/`. Each configuration includes:

- **Validation rules**: Required fields, type checks, regex patterns, expression conditions
- **Transformations**: SpEL expressions for data manipulation
- **External calls**: REST API calls with template processing
- **Conditional execution**: `when` conditions and branching logic
- **Error handling**: `stopOnValidationError` and action routing

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── example/
│   │           ├── BusinessRuleEngineApplication.java
│   │           ├── controller/
│   │           │   ├── RulesController.java
│   │           │   └── JsonController.java
│   │           ├── model/
│   │           │   ├── RuleRequest.java
│   │           │   ├── RuleResponse.java
│   │           │   ├── JsonData.java
│   │           │   └── rule/
│   │           │       ├── RuleConfig.java
│   │           │       ├── Rule.java
│   │           │       ├── ValidationRule.java
│   │           │       ├── TransformStep.java
│   │           │       ├── ExternalCall.java
│   │           │       └── Action.java
│   │           └── service/
│   │               ├── RuleEngine.java
│   │               ├── MvelEvaluator.java
│   │               ├── ExternalCallService.java
│   │               └── RuleConfigService.java
│   └── resources/
│       ├── application.properties
│       └── rules/
│           └── customer_onboard.json
├── test/
│   └── java/
│       └── com/
│           └── example/
│               └── BusinessRuleEngineApplicationTests.java
├── mock-config/
│   ├── mock-kyc.json
│   └── mock-i18n.json
├── Dockerfile
├── docker-compose.yml
└── README.md
```

## Docker Deployment

### Build and Run with Docker Compose
```bash
# Build and start all services
docker-compose up --build

# Start only the rule engine
docker-compose up business-rule-engine

# Start with mock services for testing
docker-compose up business-rule-engine mock-kyc mock-i18n
```

### Manual Docker Build
```bash
# Build the image
docker build -t business-rule-engine .

# Run the container
docker run -p 8080:8080 -e KYC_TOKEN=demo-token business-rule-engine
```
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=SpelEvaluatorTest

# Run with coverage
mvn test jacoco:report