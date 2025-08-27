package com.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public class RuleResponse {
    
    @JsonProperty("requestId")
    private String requestId;
    
    @JsonProperty("typeOfRequest")
    private String typeOfRequest;
    
    @JsonProperty("valid")
    private boolean valid;
    
    @JsonProperty("errors")
    private List<String> errors;
    
    @JsonProperty("transformedPayload")
    private Object transformedPayload;
    
    @JsonProperty("trace")
    private List<Map<String, Object>> trace;
    
    @JsonProperty("externalCalls")
    private List<Map<String, Object>> externalCalls;
    
    // Default constructor
    public RuleResponse() {}
    
    // Constructor with parameters
    public RuleResponse(String requestId, String typeOfRequest, boolean valid, 
                       List<String> errors, Object transformedPayload, 
                       List<Map<String, Object>> trace, 
                       List<Map<String, Object>> externalCalls) {
        this.requestId = requestId;
        this.typeOfRequest = typeOfRequest;
        this.valid = valid;
        this.errors = errors;
        this.transformedPayload = transformedPayload;
        this.trace = trace;
        this.externalCalls = externalCalls;
    }
    
    // Getters and Setters
    public String getRequestId() {
        return requestId;
    }
    
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
    
    public String getTypeOfRequest() {
        return typeOfRequest;
    }
    
    public void setTypeOfRequest(String typeOfRequest) {
        this.typeOfRequest = typeOfRequest;
    }
    
    public boolean isValid() {
        return valid;
    }
    
    public void setValid(boolean valid) {
        this.valid = valid;
    }
    
    public List<String> getErrors() {
        return errors;
    }
    
    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
    
    public Object getTransformedPayload() {
        return transformedPayload;
    }
    
    public void setTransformedPayload(Object transformedPayload) {
        this.transformedPayload = transformedPayload;
    }
    
    public List<Map<String, Object>> getTrace() {
        return trace;
    }
    
    public void setTrace(List<Map<String, Object>> trace) {
        this.trace = trace;
    }
    
    public List<Map<String, Object>> getExternalCalls() {
        return externalCalls;
    }
    
    public void setExternalCalls(List<Map<String, Object>> externalCalls) {
        this.externalCalls = externalCalls;
    }
    
    @Override
    public String toString() {
        return "RuleResponse{" +
                "requestId='" + requestId + '\'' +
                ", typeOfRequest='" + typeOfRequest + '\'' +
                ", valid=" + valid +
                ", errors=" + errors +
                ", transformedPayload=" + transformedPayload +
                ", trace=" + trace +
                ", externalCalls=" + externalCalls +
                '}';
    }
}
