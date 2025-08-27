package com.example.controller;

import com.example.model.RuleRequest;
import com.example.model.RuleResponse;
import com.example.model.rule.RuleConfig;
import com.example.service.RuleConfigService;
import com.example.service.RuleEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/rules")
@CrossOrigin(origins = "*")
public class RulesController {
    
    private static final Logger logger = LoggerFactory.getLogger(RulesController.class);
    private static final String API_KEY_HEADER = "X-API-KEY";
    private static final String EXPECTED_API_KEY = "change-me"; // In production, use environment variable
    
    private final RuleEngine ruleEngine;
    private final RuleConfigService ruleConfigService;
    
    public RulesController(RuleEngine ruleEngine, RuleConfigService ruleConfigService) {
        this.ruleEngine = ruleEngine;
        this.ruleConfigService = ruleConfigService;
    }
    
    @PostMapping("/consume")
    public ResponseEntity<RuleResponse> consumeRules(@RequestBody RuleRequest request,
                                                   @RequestHeader(value = API_KEY_HEADER, required = false) String apiKey) {
        
        // Validate API key
        if (apiKey == null || !EXPECTED_API_KEY.equals(apiKey)) {
            logger.warn("Invalid or missing API key");
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid or missing API key");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            logger.info("Processing rule request for type: {}", request.getTypeOfRequest());
            
            // Load rule configuration
            RuleConfig config = ruleConfigService.loadConfig(request.getTypeOfRequest());
            
            // Execute rules
            RuleResponse response = ruleEngine.execute(request, config);
            
            logger.info("Rule execution completed for requestId: {}, valid: {}", 
                       response.getRequestId(), response.isValid());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error processing rule request for type: {}", request.getTypeOfRequest(), e);
            
            RuleResponse errorResponse = new RuleResponse(
                java.util.UUID.randomUUID().toString(),
                request.getTypeOfRequest(),
                false,
                java.util.List.of("Processing failed: " + e.getMessage()),
                null,
                java.util.List.of(),
                java.util.List.of()
            );
            
            return ResponseEntity.ok(errorResponse); // Always return 200 as per requirements
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Business Rule Engine");
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/config/reload/{type}")
    public ResponseEntity<Map<String, String>> reloadConfig(@PathVariable String type,
                                                           @RequestHeader(value = API_KEY_HEADER, required = false) String apiKey) {
        
        // Validate API key
        if (apiKey == null || !EXPECTED_API_KEY.equals(apiKey)) {
            logger.warn("Invalid or missing API key for config reload");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            ruleConfigService.reloadConfig(type);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Configuration reloaded successfully for type: " + type);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error reloading configuration for type: {}", type, e);
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to reload configuration: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
