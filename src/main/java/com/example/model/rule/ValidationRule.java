package com.example.model.rule;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ValidationRule {
    
    @JsonProperty("path")
    private String path;
    
    @JsonProperty("required")
    private Boolean required;
    
    @JsonProperty("type")
    private String type;
    
    @JsonProperty("regex")
    private String regex;
    
    @JsonProperty("mvel")
    private String mvel;
    
    @JsonProperty("message")
    private String message;
    
    // Default constructor
    public ValidationRule() {}
    
    // Constructor with parameters
    public ValidationRule(String path, Boolean required, String type, 
                         String regex, String mvel, String message) {
        this.path = path;
        this.required = required;
        this.type = type;
        this.regex = regex;
        this.mvel = mvel;
        this.message = message;
    }
    
    // Getters and Setters
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
    
    public Boolean getRequired() {
        return required;
    }
    
    public void setRequired(Boolean required) {
        this.required = required;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getRegex() {
        return regex;
    }
    
    public void setRegex(String regex) {
        this.regex = regex;
    }
    
    public String getMvel() {
        return mvel;
    }
    
    public void setMvel(String mvel) {
        this.mvel = mvel;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    @Override
    public String toString() {
        return "ValidationRule{" +
                "path='" + path + '\'' +
                ", required=" + required +
                ", type='" + type + '\'' +
                ", regex='" + regex + '\'' +
                ", mvel='" + mvel + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
