package com.example.model.rule;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public class ExternalCall {
    
    @JsonProperty("method")
    private String method;
    
    @JsonProperty("url")
    private String url;
    
    @JsonProperty("headers")
    private Map<String, String> headers;
    
    @JsonProperty("bodyTemplate")
    private String bodyTemplate;
    
    @JsonProperty("saveAs")
    private String saveAs;
    
    // Default constructor
    public ExternalCall() {}
    
    // Constructor with parameters
    public ExternalCall(String method, String url, Map<String, String> headers, 
                       String bodyTemplate, String saveAs) {
        this.method = method;
        this.url = url;
        this.headers = headers;
        this.bodyTemplate = bodyTemplate;
        this.saveAs = saveAs;
    }
    
    // Getters and Setters
    public String getMethod() {
        return method;
    }
    
    public void setMethod(String method) {
        this.method = method;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public Map<String, String> getHeaders() {
        return headers;
    }
    
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
    
    public String getBodyTemplate() {
        return bodyTemplate;
    }
    
    public void setBodyTemplate(String bodyTemplate) {
        this.bodyTemplate = bodyTemplate;
    }
    
    public String getSaveAs() {
        return saveAs;
    }
    
    public void setSaveAs(String saveAs) {
        this.saveAs = saveAs;
    }
    
    @Override
    public String toString() {
        return "ExternalCall{" +
                "method='" + method + '\'' +
                ", url='" + url + '\'' +
                ", headers=" + headers +
                ", bodyTemplate='" + bodyTemplate + '\'' +
                ", saveAs='" + saveAs + '\'' +
                '}';
    }
}
