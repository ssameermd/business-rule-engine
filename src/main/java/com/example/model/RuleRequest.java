package com.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RuleRequest {
    
    @JsonProperty("typeOfRequest")
    private String typeOfRequest;
    
    @JsonProperty("payload")
    private Object payload;
    
    // Default constructor
    public RuleRequest() {}
    
    // Constructor with parameters
    public RuleRequest(String typeOfRequest, Object payload) {
        this.typeOfRequest = typeOfRequest;
        this.payload = payload;
    }
    
    // Getters and Setters
    public String getTypeOfRequest() {
        return typeOfRequest;
    }
    
    public void setTypeOfRequest(String typeOfRequest) {
        this.typeOfRequest = typeOfRequest;
    }
    
    public Object getPayload() {
        return payload;
    }
    
    public void setPayload(Object payload) {
        this.payload = payload;
    }
    
    @Override
    public String toString() {
        return "RuleRequest{" +
                "typeOfRequest='" + typeOfRequest + '\'' +
                ", payload=" + payload +
                '}';
    }
}
