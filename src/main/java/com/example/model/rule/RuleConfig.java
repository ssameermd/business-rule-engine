package com.example.model.rule;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public class RuleConfig {
    
    @JsonProperty("type")
    private String type;
    
    @JsonProperty("redactHeaders")
    private List<String> redactHeaders;
    
    @JsonProperty("defaults")
    private Map<String, Object> defaults;
    
    @JsonProperty("rules")
    private List<Rule> rules;
    
    // Default constructor
    public RuleConfig() {}
    
    // Constructor with parameters
    public RuleConfig(String type, List<String> redactHeaders, 
                     Map<String, Object> defaults, List<Rule> rules) {
        this.type = type;
        this.redactHeaders = redactHeaders;
        this.defaults = defaults;
        this.rules = rules;
    }
    
    // Getters and Setters
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public List<String> getRedactHeaders() {
        return redactHeaders;
    }
    
    public void setRedactHeaders(List<String> redactHeaders) {
        this.redactHeaders = redactHeaders;
    }
    
    public Map<String, Object> getDefaults() {
        return defaults;
    }
    
    public void setDefaults(Map<String, Object> defaults) {
        this.defaults = defaults;
    }
    
    public List<Rule> getRules() {
        return rules;
    }
    
    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }
    
    @Override
    public String toString() {
        return "RuleConfig{" +
                "type='" + type + '\'' +
                ", redactHeaders=" + redactHeaders +
                ", defaults=" + defaults +
                ", rules=" + rules +
                '}';
    }
}
