package com.example.model.rule;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TransformStep {
    
    @JsonProperty("kind")
    private String kind;
    
    @JsonProperty("spel")
    private String spel;
    
    // Default constructor
    public TransformStep() {}
    
    // Constructor with parameters
    public TransformStep(String kind, String spel) {
        this.kind = kind;
        this.spel = spel;
    }
    
    // Getters and Setters
    public String getKind() {
        return kind;
    }
    
    public void setKind(String kind) {
        this.kind = kind;
    }
    
    public String getSpel() { return spel; }
    public void setSpel(String spel) { this.spel = spel; }
    
    @Override
    public String toString() {
        return "TransformStep{" +
                "kind='" + kind + '\'' +
                ", spel='" + spel + '\'' +
                '}';
    }
}
