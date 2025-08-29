package com.example.service;

import com.example.model.RuleRequest;
import com.example.model.RuleResponse;
import com.example.model.rule.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class RuleEngine {
    
    private static final Logger logger = LoggerFactory.getLogger(RuleEngine.class);
    private final SpelEvaluator spelEvaluator;
    private final ExternalCallService externalCallService;
    private final ObjectMapper objectMapper;
    
    public RuleEngine(SpelEvaluator spelEvaluator, 
                     ExternalCallService externalCallService, 
                     ObjectMapper objectMapper) {
        this.spelEvaluator = spelEvaluator;
        this.externalCallService = externalCallService;
        this.objectMapper = objectMapper;
    }
    
    public RuleResponse execute(RuleRequest request, RuleConfig config) {
        String requestId = UUID.randomUUID().toString();
        logger.info("Starting rule execution for requestId: {}, type: {}", requestId, request.getTypeOfRequest());
        
        List<String> errors = new ArrayList<>();
        List<Map<String, Object>> trace = new ArrayList<>();
        List<Map<String, Object>> externalCalls = new ArrayList<>();
        
        try {
            // Initialize context
            Map<String, Object> ctx = new HashMap<>();
            Map<String, Object> payload = objectMapper.convertValue(request.getPayload(), Map.class);
            Map<String, Object> defaults = config.getDefaults() != null ? config.getDefaults() : new HashMap<>();
            Map<String, String> env = System.getenv();
            
            // Execute rules
            for (Rule rule : config.getRules()) {
                Map<String, Object> ruleTrace = new HashMap<>();
                ruleTrace.put("ruleId", rule.getId());
                ruleTrace.put("description", rule.getDescription());
                ruleTrace.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                
                try {
                    // Check when condition
                    if (rule.getWhen() != null && !rule.getWhen().trim().isEmpty()) {
                        Map<String, Object> context = spelEvaluator.createContext(payload, ctx, defaults, env);
                        boolean whenCondition = spelEvaluator.evaluateBoolean(rule.getWhen(), context);
                        if (!whenCondition) {
                            ruleTrace.put("status", "SKIPPED");
                            ruleTrace.put("reason", "when condition not met");
                            trace.add(ruleTrace);
                            continue;
                        }
                    }
                    
                    // Validate
                    if (rule.getValidate() != null && !rule.getValidate().isEmpty()) {
                        List<String> validationErrors = validate(rule.getValidate(), payload, ctx, defaults, env);
                        if (!validationErrors.isEmpty()) {
                            errors.addAll(validationErrors);
                            ruleTrace.put("status", "FAILED");
                            ruleTrace.put("errors", validationErrors);
                            trace.add(ruleTrace);
                            
                            if (Boolean.TRUE.equals(rule.getStopOnValidationError())) {
                                break;
                            }
                            continue;
                        }
                    }
                    
                    // External call
                    if (rule.getExternalCall() != null) {
                        Map<String, Object> context = spelEvaluator.createContext(payload, ctx, defaults, env);
                        Object result = null;
                        String errorMessage = null;
                        try {
                            result = externalCallService.invoke(rule.getExternalCall(), context);
                            logger.info("External call result for rule {}: {}", rule.getId(), result);
                        } catch (Exception e) {
                            logger.warn("External call failed for rule {}: {}", rule.getId(), e.getMessage());
                            result = null;
                            errorMessage = e.getMessage();
                        }
                        
                        // Save result to context for internal use in transformations
                        if (rule.getExternalCall().getSaveAs() != null) {
                            ctx.put(rule.getExternalCall().getSaveAs(), result);
                            logger.info("Saved external call result to context with key '{}': {}", rule.getExternalCall().getSaveAs(), result);
                        }
                        
                        // Record external call metadata (without the actual response data)
                        Map<String, Object> externalCall = new HashMap<>();
                        externalCall.put("ruleId", rule.getId());
                        externalCall.put("url", rule.getExternalCall().getUrl());
                        externalCall.put("method", rule.getExternalCall().getMethod());
                        externalCall.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                        externalCall.put("status", result != null ? "SUCCESS" : "FAILED");
                        if (errorMessage != null) {
                            externalCall.put("error", errorMessage);
                        }
                        // Note: We don't include the actual result data to avoid exposing external API responses
                        externalCalls.add(externalCall);
                        
                        ruleTrace.put("status", result != null ? "EXTERNAL_CALL" : "EXTERNAL_CALL_FAILED");
                        ruleTrace.put("externalCall", externalCall);
                    }
                    
                    // Transform - use the same context that may contain external call results
                    if (rule.getTransform() != null && !rule.getTransform().isEmpty()) {
                        Map<String, Object> context = spelEvaluator.createContext(payload, ctx, defaults, env);
                        for (TransformStep step : rule.getTransform()) {
                            if ("SPEL".equalsIgnoreCase(step.getKind())) {
                                spelEvaluator.evaluate(step.getSpel(), context);
                            }
                        }
                        ruleTrace.put("status", "TRANSFORMED");
                    }
                    
                    // Check action
                    if (rule.getOnSuccess() != null && "STOP".equals(rule.getOnSuccess().getAction())) {
                        ruleTrace.put("status", "STOPPED");
                        trace.add(ruleTrace);
                        break;
                    }
                    
                    if (ruleTrace.get("status") == null) {
                        ruleTrace.put("status", "SUCCESS");
                    }
                    
                } catch (Exception e) {
                    logger.error("Error executing rule: {}", rule.getId(), e);
                    ruleTrace.put("status", "ERROR");
                    ruleTrace.put("error", e.getMessage());
                    errors.add("Rule " + rule.getId() + " failed: " + e.getMessage());
                }
                
                trace.add(ruleTrace);
            }
            
            return new RuleResponse(requestId, request.getTypeOfRequest(), errors.isEmpty(), 
                                  errors, payload, trace, externalCalls);
            
        } catch (Exception e) {
            logger.error("Error in rule execution for requestId: {}", requestId, e);
            errors.add("Rule execution failed: " + e.getMessage());
            return new RuleResponse(requestId, request.getTypeOfRequest(), false, 
                                  errors, null, trace, externalCalls);
        }
    }
    
    private List<String> validate(List<ValidationRule> validationRules, Map<String, Object> payload, 
                                Map<String, Object> ctx, Map<String, Object> defaults, 
                                Map<String, String> env) {
        List<String> errors = new ArrayList<>();
        Map<String, Object> context = spelEvaluator.createContext(payload, ctx, defaults, env);
        
        for (ValidationRule validation : validationRules) {
            try {
                Object value = getValueByPath(payload, validation.getPath());
                
                // Required check - check for null, empty string, or blank string
                if (Boolean.TRUE.equals(validation.getRequired()) && isBlank(value)) {
                    errors.add(validation.getMessage());
                    continue;
                }
                
                if (value == null) {
                    continue; // Skip other validations if value is null and not required
                }
                
                // Type check
                if (validation.getType() != null) {
                    if (!isValidType(value, validation.getType())) {
                        errors.add(validation.getMessage());
                        continue;
                    }
                }
                
                // Regex check
                if (validation.getRegex() != null) {
                    if (!(value instanceof String) || !Pattern.matches(validation.getRegex(), (String) value)) {
                        errors.add(validation.getMessage());
                        continue;
                    }
                }
                
                // SpEL check
                if (validation.getSpel() != null) {
                    if (!spelEvaluator.evaluateBoolean(validation.getSpel(), context)) {
                        errors.add(validation.getMessage());
                    }
                }
                
            } catch (Exception e) {
                logger.error("Error in validation: {}", validation.getPath(), e);
                errors.add("Validation error for " + validation.getPath() + ": " + e.getMessage());
            }
        }
        
        return errors;
    }
    
    public Object getValueByPath(Map<String, Object> payload, String path) {
        if (path == null || path.trim().isEmpty()) {
            return payload;
        }
        
        // Simple path resolution for $.field format
        if (path.startsWith("$.")) {
            String field = path.substring(2);
            return payload.get(field);
        }
        
        return payload;
    }
    
    /**
     * Checks if a value is of the expected type.
     * Note: Empty strings are considered valid strings (use required validation for non-empty checks).
     */
    public boolean isValidType(Object value, String expectedType) {
        if (value == null) {
            return false; // null is not valid for any type
        }
        
        switch (expectedType.toLowerCase()) {
            case "string":
                return value instanceof String;
            case "number":
                return value instanceof Number;
            case "boolean":
                return value instanceof Boolean;
            case "object":
                return value instanceof Map;
            case "array":
                return value instanceof List;
            default:
                return true; // Unknown type, assume valid
        }
    }

    private boolean isBlank(Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof String) {
            return ((String) value).trim().isEmpty();
        }
        return false;
    }
    
    /**
     * Sanitizes sensitive data from results.
     * Note: External call results are not included in responses to avoid exposing external API data.
     */
    private Object sanitizeResult(Object result, List<String> redactHeaders) {
        if (result == null) {
            return null;
        }
        
        try {
            // Convert to JSON and back to sanitize sensitive data
            String json = objectMapper.writeValueAsString(result);
            Map<String, Object> sanitized = objectMapper.readValue(json, Map.class);
            
            if (redactHeaders != null && sanitized instanceof Map) {
                for (String header : redactHeaders) {
                    if (sanitized.containsKey(header)) {
                        sanitized.put(header, "***REDACTED***");
                    }
                }
            }
            
            return sanitized;
        } catch (Exception e) {
            logger.warn("Could not sanitize result", e);
            return result;
        }
    }
}
