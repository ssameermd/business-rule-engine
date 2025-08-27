# Business Rule Engine - Spring Boot REST API

A basic Spring Boot application with REST controllers to consume JSON data and print it to the console.

## Features

- REST API endpoints to consume JSON data
- Multiple ways to handle JSON input (structured objects, raw maps, strings)
- Health check endpoint
- Console logging of received data

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

### 1. Health Check
- **GET** `/api/health`
- Returns application status

### 2. Consume JSON (Enhanced)
- **POST** `/api/consume`
- Accepts any JSON structure as a string and parses it using `org.json.JSONObject`
- Handles exceptions gracefully with detailed error messages
- Example request body:
```json
{
  "message": "Hello World",
  "data": {"key": "value", "number": 42},
  "timestamp": "2024-01-01T12:00:00"
}
```

### 3. Consume Raw JSON
- **POST** `/api/consume-raw`
- Accepts any JSON structure as a string and parses it using `org.json.JSONObject`
- Simplified parsing without field-specific handling
- Example request body:
```json
{
  "name": "John Doe",
  "age": 30,
  "city": "New York"
}
```

### 4. Validate JSON String
- **POST** `/api/consume-string`
- Accepts JSON as a string and validates its format
- Returns validation result
- Example request body:
```json
"{\"message\": \"Hello\", \"value\": 123}"
```

## Testing with curl

### Health Check
```bash
curl -X GET http://localhost:8080/api/health
```

### Consume JSON (Enhanced)
```bash
curl -X POST http://localhost:8080/api/consume \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Test message",
    "data": {"test": "value"},
    "timestamp": "2024-01-01T12:00:00"
  }'
```

### Consume Raw JSON
```bash
curl -X POST http://localhost:8080/api/consume-raw \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John",
    "age": 25,
    "city": "Boston"
  }'
```

### Validate JSON String
```bash
curl -X POST http://localhost:8080/api/consume-string \
  -H "Content-Type: application/json" \
  -d '"{\"message\": \"Hello World\"}"'
```

### Test Invalid JSON (Error Handling)
```bash
curl -X POST http://localhost:8080/api/consume \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Invalid JSON",
    "data": {"test": "value"
  }'
```

## Console Output

All received JSON data will be parsed using `org.json.JSONObject` and printed to the console with clear formatting and separators for easy reading. The application includes comprehensive error handling:

- **Valid JSON**: Parsed and displayed with pretty formatting
- **Invalid JSON**: Detailed error messages with the received data
- **Unexpected errors**: Graceful handling with appropriate HTTP status codes

### Example Console Output for Valid JSON:
```
=== Received JSON String ===
Raw JSON String: {"message":"Hello World","data":{"key":"value"},"timestamp":"2024-01-01T12:00:00"}
=== Parsed JSON Object ===
JSON Object: {
  "message": "Hello World",
  "data": {
    "key": "value"
  },
  "timestamp": "2024-01-01T12:00:00"
}
=== All Fields ===
message: Hello World
data: {"key":"value"}
timestamp: 2024-01-01T12:00:00
==========================
```

### Example Console Output for Invalid JSON:
```
=== JSON Parsing Error ===
Error: Expected a ',' or '}' at 25 [character 26 line 1]
Invalid JSON: {"message":"Invalid","data":{"test":"value"}
==========================
```

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── example/
│   │           ├── BusinessRuleEngineApplication.java
│   │           ├── controller/
│   │           │   └── JsonController.java
│   │           └── model/
│   │               └── JsonData.java
│   └── resources/
│       └── application.properties
└── test/
    └── java/
        └── com/
            └── example/
                └── BusinessRuleEngineApplicationTests.java
```
