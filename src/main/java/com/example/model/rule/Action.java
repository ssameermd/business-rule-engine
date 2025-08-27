package com.example.model.rule;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Action {
    
    @JsonProperty("action")
    private String action;
    
    // Default constructor
    public Action() {}
    
    // Constructor with parameters
    public Action(String action) {
        this.action = action;
    }
    
    // Getters and Setters
    public String getAction() {
        return action;
    }
    
    public void setAction(String action) {
        this.action = action;
    }
    
    @Override
    public String toString() {
        return "Action{" +
                "action='" + action + '\'' +
                '}';
    }
}
