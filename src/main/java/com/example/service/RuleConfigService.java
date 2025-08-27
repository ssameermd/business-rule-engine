package com.example.service;

import com.example.model.rule.RuleConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RuleConfigService {
    
    private static final Logger logger = LoggerFactory.getLogger(RuleConfigService.class);
    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, RuleConfig> configCache = new ConcurrentHashMap<>();
    
    public RuleConfigService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    public RuleConfig loadConfig(String typeOfRequest) {
        return configCache.computeIfAbsent(typeOfRequest, this::loadConfigFromFile);
    }
    
    private RuleConfig loadConfigFromFile(String typeOfRequest) {
        try {
            String configPath = "rules/" + typeOfRequest + ".json";
            ClassPathResource resource = new ClassPathResource(configPath);
            
            if (!resource.exists()) {
                throw new RuntimeException("Rule configuration not found for type: " + typeOfRequest);
            }
            
            try (InputStream inputStream = resource.getInputStream()) {
                RuleConfig config = objectMapper.readValue(inputStream, RuleConfig.class);
                logger.info("Loaded rule configuration for type: {}", typeOfRequest);
                return config;
            }
            
        } catch (IOException e) {
            logger.error("Error loading rule configuration for type: {}", typeOfRequest, e);
            throw new RuntimeException("Failed to load rule configuration: " + e.getMessage(), e);
        }
    }
    
    public void clearCache() {
        configCache.clear();
        logger.info("Rule configuration cache cleared");
    }
    
    public void reloadConfig(String typeOfRequest) {
        configCache.remove(typeOfRequest);
        loadConfig(typeOfRequest);
        logger.info("Reloaded rule configuration for type: {}", typeOfRequest);
    }
}
