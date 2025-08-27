package com.example.model.rule;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TransformStep {
    
    @JsonProperty("kind")
    private String kind;
    
    @JsonProperty("mvel")
    private String mvel;
    
    // Default constructor
    public TransformStep() {}
    
    // Constructor with parameters
    public TransformStep(String kind, String mvel) {
        this.kind = kind;
        this.mvel = mvel;
    }
    
    // Getters and Setters
    public String getKind() {
        return kind;
    }
    
    public void setKind(String kind) {
        this.kind = kind;
    }
    
    public String getMvel() {
        return mvel;
    }
    
    public void setMvel(String mvel) {
        this.mvel = mvel;
    }
    
    @Override
    public String toString() {
        return "TransformStep{" +
                "kind='" + kind + '\'' +
                ", mvel='" + mvel + '\'' +
                '}';
    }
}
