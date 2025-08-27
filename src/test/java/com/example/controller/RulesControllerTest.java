package com.example.controller;

import com.example.model.RuleRequest;
import com.example.model.RuleResponse;
import com.example.model.rule.RuleConfig;
import com.example.service.RuleConfigService;
import com.example.service.RuleEngine;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RulesController.class)
class RulesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RuleEngine ruleEngine;

    @MockBean
    private RuleConfigService ruleConfigService;

    @Autowired
    private ObjectMapper objectMapper;

    private RuleRequest validRequest;
    private RuleResponse validResponse;

    @BeforeEach
    void setUp() {
        // Setup valid request
        validRequest = new RuleRequest();
        validRequest.setTypeOfRequest("customer_onboard");
        Map<String, Object> payload = new HashMap<>();
        payload.put("firstName", "John");
        payload.put("lastName", "Doe");
        payload.put("email", "john.doe@example.com");
        payload.put("age", 25);
        payload.put("country", "US");
        validRequest.setPayload(payload);

        // Setup valid response
        validResponse = new RuleResponse();
        validResponse.setRequestId("test-request-id");
        validResponse.setTypeOfRequest("customer_onboard");
        validResponse.setValid(true);
        validResponse.setErrors(new java.util.ArrayList<>());
        validResponse.setTransformedPayload(payload);
        validResponse.setTrace(new java.util.ArrayList<>());
        validResponse.setExternalCalls(new java.util.ArrayList<>());
    }

    @Test
    void testConsumeRulesSuccess() throws Exception {
        // Given
        RuleConfig config = new RuleConfig();
        config.setType("customer_onboard");

        when(ruleConfigService.loadConfig("customer_onboard")).thenReturn(config);
        when(ruleEngine.execute(any(RuleRequest.class), eq(config))).thenReturn(validResponse);

        // When & Then
        mockMvc.perform(post("/rules/consume")
                .header("X-API-KEY", "change-me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value("test-request-id"))
                .andExpect(jsonPath("$.typeOfRequest").value("customer_onboard"))
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    void testConsumeRulesMissingApiKey() throws Exception {
        // When & Then
        mockMvc.perform(post("/rules/consume")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testConsumeRulesInvalidApiKey() throws Exception {
        // When & Then
        mockMvc.perform(post("/rules/consume")
                .header("X-API-KEY", "invalid-key")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testConsumeRulesInvalidRequest() throws Exception {
        // Given
        RuleRequest invalidRequest = new RuleRequest();
        // Missing required fields

        // When & Then
        mockMvc.perform(post("/rules/consume")
                .header("X-API-KEY", "change-me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testConsumeRulesConfigNotFound() throws Exception {
        // Given
        when(ruleConfigService.loadConfig("unknown_type")).thenThrow(new RuntimeException("Config not found"));

        RuleRequest request = new RuleRequest();
        request.setTypeOfRequest("unknown_type");
        request.setPayload(new HashMap<>());

        // When & Then
        mockMvc.perform(post("/rules/consume")
                .header("X-API-KEY", "change-me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testHealthCheck() throws Exception {
        // When & Then
        mockMvc.perform(get("/rules/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void testReloadConfigSuccess() throws Exception {
        // Given
        doNothing().when(ruleConfigService).reloadConfig("customer_onboard");

        // When & Then
        mockMvc.perform(post("/rules/config/reload/customer_onboard")
                .header("X-API-KEY", "change-me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Configuration reloaded successfully"))
                .andExpect(jsonPath("$.type").value("customer_onboard"));
    }

    @Test
    void testReloadConfigMissingApiKey() throws Exception {
        // When & Then
        mockMvc.perform(post("/rules/config/reload/customer_onboard"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testReloadConfigInvalidApiKey() throws Exception {
        // When & Then
        mockMvc.perform(post("/rules/config/reload/customer_onboard")
                .header("X-API-KEY", "invalid-key"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testReloadConfigFailure() throws Exception {
        // Given
        doNothing().when(ruleConfigService).reloadConfig("unknown_type");

        // When & Then
        mockMvc.perform(post("/rules/config/reload/unknown_type")
                .header("X-API-KEY", "change-me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Configuration reloaded successfully"))
                .andExpect(jsonPath("$.type").value("unknown_type"));
    }

    @Test
    void testConsumeRulesWithValidationFailure() throws Exception {
        // Given
        RuleConfig config = new RuleConfig();
        config.setType("customer_onboard");

        RuleResponse failedResponse = new RuleResponse();
        failedResponse.setRequestId("test-request-id");
        failedResponse.setTypeOfRequest("customer_onboard");
        failedResponse.setValid(false);
        failedResponse.setErrors(java.util.Arrays.asList("Validation failed"));
        failedResponse.setTransformedPayload(new HashMap<>());
        failedResponse.setTrace(new java.util.ArrayList<>());
        failedResponse.setExternalCalls(new java.util.ArrayList<>());

        when(ruleConfigService.loadConfig("customer_onboard")).thenReturn(config);
        when(ruleEngine.execute(any(RuleRequest.class), eq(config))).thenReturn(failedResponse);

        // When & Then
        mockMvc.perform(post("/rules/consume")
                .header("X-API-KEY", "change-me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0]").value("Validation failed"));
    }

    @Test
    void testConsumeRulesWithComplexPayload() throws Exception {
        // Given
        RuleConfig config = new RuleConfig();
        config.setType("customer_onboard");

        Map<String, Object> complexPayload = new HashMap<>();
        complexPayload.put("firstName", "John");
        complexPayload.put("lastName", "Doe");
        complexPayload.put("email", "john.doe@example.com");
        complexPayload.put("age", 25);
        complexPayload.put("country", "US");
        complexPayload.put("address", Map.of(
            "street", "123 Main St",
            "city", "New York",
            "zipCode", "10001"
        ));
        complexPayload.put("preferences", Map.of(
            "newsletter", true,
            "notifications", false
        ));

        RuleRequest complexRequest = new RuleRequest();
        complexRequest.setTypeOfRequest("customer_onboard");
        complexRequest.setPayload(complexPayload);

        when(ruleConfigService.loadConfig("customer_onboard")).thenReturn(config);
        when(ruleEngine.execute(any(RuleRequest.class), eq(config))).thenReturn(validResponse);

        // When & Then
        mockMvc.perform(post("/rules/consume")
                .header("X-API-KEY", "change-me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(complexRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true));
    }
}
