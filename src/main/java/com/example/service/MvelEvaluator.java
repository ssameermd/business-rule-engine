package com.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mvel2.MVEL;
import org.mvel2.ParserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class MvelEvaluator {
    
    private static final Logger logger = LoggerFactory.getLogger(MvelEvaluator.class);
    private static final Pattern MVEL_TEMPLATE_PATTERN = Pattern.compile("\\{\\{mvel:\\s*(.*?)\\s*\\}\\}");
    private final ObjectMapper objectMapper;
    
    public MvelEvaluator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    /**
     * Evaluate MVEL expression with context variables
     */
    public Object evaluate(String expression, Map<String, Object> context) {
        try {
            if (expression == null || expression.trim().isEmpty()) {
                return null;
            }
            
            ParserContext parserContext = new ParserContext();
            parserContext.addImport("java.util", java.util.Map.class);
            parserContext.addImport("java.time", java.time.LocalDateTime.class);
            
            return MVEL.eval(expression, context);
        } catch (Exception e) {
            logger.error("Error evaluating MVEL expression: {}", expression, e);
            throw new RuntimeException("MVEL evaluation failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Evaluate boolean MVEL expression
     */
    public boolean evaluateBoolean(String expression, Map<String, Object> context) {
        Object result = evaluate(expression, context);
        if (result instanceof Boolean) {
            return (Boolean) result;
        }
        return false;
    }
    
    /**
     * Process template with MVEL expressions
     */
    public String processTemplate(String template, Map<String, Object> context) {
        if (template == null) {
            return null;
        }
        
        Matcher matcher = MVEL_TEMPLATE_PATTERN.matcher(template);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String mvelExpression = matcher.group(1).trim();
            Object evaluated = evaluate(mvelExpression, context);
            String replacement = evaluated != null ? evaluated.toString() : "";
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
    
    /**
     * Create context map with all required variables
     */
    public Map<String, Object> createContext(Object payload, Map<String, Object> ctx, 
                                           Map<String, Object> defaults, 
                                           Map<String, String> env) {
        Map<String, Object> context = new HashMap<>();
        context.put("payload", payload);
        context.put("ctx", ctx);
        context.put("defaults", defaults);
        context.put("env", env);
        context.put("now", java.time.LocalDateTime.now());
        return context;
    }
}
