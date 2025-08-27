package com.example.service;

import com.example.model.rule.ExternalCall;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ExternalCallService {
    
    private static final Logger logger = LoggerFactory.getLogger(ExternalCallService.class);
    private final RestTemplateBuilder restTemplateBuilder;
    private final MvelEvaluator mvelEvaluator;
    private final ObjectMapper objectMapper;
    
    public ExternalCallService(RestTemplateBuilder restTemplateBuilder, 
                             MvelEvaluator mvelEvaluator, 
                             ObjectMapper objectMapper) {
        this.restTemplateBuilder = restTemplateBuilder;
        this.mvelEvaluator = mvelEvaluator;
        this.objectMapper = objectMapper;
    }
    
    public Object invoke(ExternalCall call, Map<String, Object> context) {
        try {
            String method = call.getMethod();
            String url = call.getUrl();
            
            // Process URL template if it contains MVEL expressions
            url = mvelEvaluator.processTemplate(url, context);
            
            if ("GET".equalsIgnoreCase(method)) {
                return handleGet(url, call, context);
            } else if ("POST".equalsIgnoreCase(method)) {
                return handlePost(url, call, context);
            } else {
                logger.warn("Unsupported HTTP method: {}", method);
                return null;
            }
        } catch (Exception e) {
            logger.error("Error making external call: {}", call.getUrl(), e);
            throw new RuntimeException("External call failed: " + e.getMessage(), e);
        }
    }
    
    private Object handleGet(String url, ExternalCall call, Map<String, Object> context) {
        var restTemplate = restTemplateBuilder.build();
        
        // Process headers if they contain MVEL expressions
        HttpHeaders headers = new HttpHeaders();
        if (call.getHeaders() != null) {
            for (Map.Entry<String, String> header : call.getHeaders().entrySet()) {
                String headerValue = mvelEvaluator.processTemplate(header.getValue(), context);
                headers.set(header.getKey(), headerValue);
            }
        }
        
        HttpEntity<String> request = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);
        
        logger.info("GET call to {} returned status: {}", url, response.getStatusCode());
        return response.getBody();
    }
    
    private Object handlePost(String url, ExternalCall call, Map<String, Object> context) {
        var restTemplate = restTemplateBuilder.build();
        
        // Process headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (call.getHeaders() != null) {
            for (Map.Entry<String, String> header : call.getHeaders().entrySet()) {
                String headerValue = mvelEvaluator.processTemplate(header.getValue(), context);
                headers.set(header.getKey(), headerValue);
            }
        }
        
        // Process body template
        String body = call.getBodyTemplate();
        if (body != null) {
            body = mvelEvaluator.processTemplate(body, context);
        }
        
        HttpEntity<String> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);
        
        logger.info("POST call to {} returned status: {}", url, response.getStatusCode());
        return response.getBody();
    }
}
