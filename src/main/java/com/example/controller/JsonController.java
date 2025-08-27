package com.example.controller;

import com.example.model.JsonData;
import org.json.JSONObject;
import org.json.JSONException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class JsonController {

    @PostMapping("/consume")
    public ResponseEntity<String> consumeJson(@RequestBody String jsonString) {
        try {
            System.out.println("=== Received JSON String ===");
            System.out.println("Raw JSON String: " + jsonString);
            
            // Parse JSON string using org.json.JSONObject
            JSONObject jsonObject = new JSONObject(jsonString);
            
            System.out.println("=== Parsed JSON Object ===");
            System.out.println("JSON Object: " + jsonObject.toString(2)); // Pretty print with 2 spaces indentation
            
            // Print individual fields if they exist
            if (jsonObject.has("message")) {
                System.out.println("Message: " + jsonObject.getString("message"));
            }
            if (jsonObject.has("data")) {
                System.out.println("Data: " + jsonObject.get("data"));
            }
            if (jsonObject.has("timestamp")) {
                System.out.println("Timestamp: " + jsonObject.getString("timestamp"));
            }
            
            // Print all keys and values
            System.out.println("=== All Fields ===");
            for (String key : jsonObject.keySet()) {
                System.out.println(key + ": " + jsonObject.get(key));
            }
            System.out.println("==========================");
            
            return ResponseEntity.ok("JSON data received and parsed successfully!");
            
        } catch (JSONException e) {
            System.err.println("=== JSON Parsing Error ===");
            System.err.println("Error: " + e.getMessage());
            System.err.println("Invalid JSON: " + jsonString);
            System.err.println("==========================");
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid JSON format");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("received", jsonString);
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new JSONObject(errorResponse).toString());
                    
        } catch (Exception e) {
            System.err.println("=== Unexpected Error ===");
            System.err.println("Error: " + e.getMessage());
            System.err.println("Received data: " + jsonString);
            System.err.println("==========================");
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Unexpected error occurred");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new JSONObject(errorResponse).toString());
        }
    }
    
    @PostMapping("/consume-raw")
    public ResponseEntity<String> consumeRawJson(@RequestBody String jsonString) {
        try {
            System.out.println("=== Received Raw JSON String ===");
            System.out.println("Raw JSON String: " + jsonString);
            
            // Parse JSON string using org.json.JSONObject
            JSONObject jsonObject = new JSONObject(jsonString);
            
            System.out.println("=== Parsed JSON Object ===");
            System.out.println("JSON Object: " + jsonObject.toString(2));
            System.out.println("===============================");
            
            return ResponseEntity.ok("Raw JSON data received and parsed successfully!");
            
        } catch (JSONException e) {
            System.err.println("=== JSON Parsing Error ===");
            System.err.println("Error: " + e.getMessage());
            System.err.println("Invalid JSON: " + jsonString);
            System.err.println("==========================");
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid JSON format");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("received", jsonString);
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new JSONObject(errorResponse).toString());
        }
    }
    
    @PostMapping("/consume-string")
    public ResponseEntity<String> consumeJsonString(@RequestBody String jsonString) {
        try {
            System.out.println("=== Received JSON String ===");
            System.out.println("JSON String: " + jsonString);
            
            // Validate if it's a valid JSON string
            JSONObject jsonObject = new JSONObject(jsonString);
            System.out.println("Valid JSON confirmed");
            System.out.println("=============================");
            
            return ResponseEntity.ok("JSON string received and validated successfully!");
            
        } catch (JSONException e) {
            System.err.println("=== JSON Validation Error ===");
            System.err.println("Error: " + e.getMessage());
            System.err.println("Invalid JSON string: " + jsonString);
            System.err.println("=============================");
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid JSON string");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("received", jsonString);
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new JSONObject(errorResponse).toString());
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = Map.of(
            "status", "UP",
            "timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            "message", "Business Rule Engine is running"
        );
        return ResponseEntity.ok(response);
    }
}
