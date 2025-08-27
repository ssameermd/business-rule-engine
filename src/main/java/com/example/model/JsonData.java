package com.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class JsonData {
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("data")
    private Object data;
    
    @JsonProperty("timestamp")
    private String timestamp;
    
    // Default constructor
    public JsonData() {}
    
    // Constructor with parameters
    public JsonData(String message, Object data, String timestamp) {
        this.message = message;
        this.data = data;
        this.timestamp = timestamp;
    }
    
    // Getters and Setters
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public Object getData() {
        return data;
    }
    
    public void setData(Object data) {
        this.data = data;
    }
    
    public String getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    
    @Override
    public String toString() {
        return "JsonData{" +
                "message='" + message + '\'' +
                ", data=" + data +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
