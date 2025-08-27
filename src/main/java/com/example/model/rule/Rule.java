package com.example.model.rule;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public class Rule {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("when")
    private String when;
    
    @JsonProperty("stopOnValidationError")
    private Boolean stopOnValidationError;
    
    @JsonProperty("validate")
    private List<ValidationRule> validate;
    
    @JsonProperty("transform")
    private List<TransformStep> transform;
    
    @JsonProperty("externalCall")
    private ExternalCall externalCall;
    
    @JsonProperty("onSuccess")
    private Action onSuccess;
    
    @JsonProperty("onFailure")
    private Action onFailure;
    
    // Default constructor
    public Rule() {}
    
    // Constructor with parameters
    public Rule(String id, String description, String when, Boolean stopOnValidationError,
                List<ValidationRule> validate, List<TransformStep> transform,
                ExternalCall externalCall, Action onSuccess, Action onFailure) {
        this.id = id;
        this.description = description;
        this.when = when;
        this.stopOnValidationError = stopOnValidationError;
        this.validate = validate;
        this.transform = transform;
        this.externalCall = externalCall;
        this.onSuccess = onSuccess;
        this.onFailure = onFailure;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getWhen() {
        return when;
    }
    
    public void setWhen(String when) {
        this.when = when;
    }
    
    public Boolean getStopOnValidationError() {
        return stopOnValidationError;
    }
    
    public void setStopOnValidationError(Boolean stopOnValidationError) {
        this.stopOnValidationError = stopOnValidationError;
    }
    
    public List<ValidationRule> getValidate() {
        return validate;
    }
    
    public void setValidate(List<ValidationRule> validate) {
        this.validate = validate;
    }
    
    public List<TransformStep> getTransform() {
        return transform;
    }
    
    public void setTransform(List<TransformStep> transform) {
        this.transform = transform;
    }
    
    public ExternalCall getExternalCall() {
        return externalCall;
    }
    
    public void setExternalCall(ExternalCall externalCall) {
        this.externalCall = externalCall;
    }
    
    public Action getOnSuccess() {
        return onSuccess;
    }
    
    public void setOnSuccess(Action onSuccess) {
        this.onSuccess = onSuccess;
    }
    
    public Action getOnFailure() {
        return onFailure;
    }
    
    public void setOnFailure(Action onFailure) {
        this.onFailure = onFailure;
    }
    
    @Override
    public String toString() {
        return "Rule{" +
                "id='" + id + '\'' +
                ", description='" + description + '\'' +
                ", when='" + when + '\'' +
                ", stopOnValidationError=" + stopOnValidationError +
                ", validate=" + validate +
                ", transform=" + transform +
                ", externalCall=" + externalCall +
                ", onSuccess=" + onSuccess +
                ", onFailure=" + onFailure +
                '}';
    }
}
