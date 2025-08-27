package com.example;

import com.example.model.RuleRequest;
import com.example.model.RuleResponse;
import com.example.service.ExternalCallService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
class BusinessRuleEngineApplicationTests {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ExternalCallService externalCallService;

    private MockMvc mockMvc;

    @Test
    void contextLoads() {
        assertNotNull(webApplicationContext);
    }

    @Test
    void testHealthEndpoint() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        mockMvc.perform(get("/rules/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void testCustomerOnboardRuleProcessing() throws Exception {
        // Mock external call service to return a valid response
        Map<String, Object> mockI18nResult = new HashMap<>();
        Map<String, Object> allocation = new HashMap<>();
        allocation.put("AllocatetheApprovers", "Mock Allocation Message");
        mockI18nResult.put("ALLOCATION", allocation);
        when(externalCallService.invoke(any(), any())).thenReturn(mockI18nResult);

        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        // Create test request
        RuleRequest request = new RuleRequest();
        request.setTypeOfRequest("customer_onboard");
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("firstName", "John");
        payload.put("lastName", "Doe");
        payload.put("email", "john.doe@example.com");
        payload.put("age", 25);
        payload.put("country", "US");
        request.setPayload(payload);

        // Test the rule processing endpoint
        mockMvc.perform(post("/rules/consume")
                .header("X-API-KEY", "change-me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").exists())
                .andExpect(jsonPath("$.typeOfRequest").value("customer_onboard"))
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.trace").isArray())
                .andExpect(jsonPath("$.externalCalls").isArray())
                .andExpect(jsonPath("$.transformedPayload.fullName").value("John Doe"))
                .andExpect(jsonPath("$.transformedPayload.riskScore").exists())
                .andExpect(jsonPath("$.transformedPayload.processedAt").exists());
    }

    @Test
    void testCustomerOnboardRuleProcessingWithExternalCallFailure() throws Exception {
        // Mock external call service to throw an exception
        when(externalCallService.invoke(any(), any())).thenThrow(new RuntimeException("SSL Certificate Error"));

        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        // Create test request
        RuleRequest request = new RuleRequest();
        request.setTypeOfRequest("customer_onboard");
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("firstName", "John");
        payload.put("lastName", "Doe");
        payload.put("email", "john.doe@example.com");
        payload.put("age", 25);
        payload.put("country", "US");
        request.setPayload(payload);

        // Test the rule processing endpoint - should still succeed despite external call failure
        mockMvc.perform(post("/rules/consume")
                .header("X-API-KEY", "change-me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").exists())
                .andExpect(jsonPath("$.typeOfRequest").value("customer_onboard"))
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.trace").isArray())
                .andExpect(jsonPath("$.externalCalls").isArray())
                .andExpect(jsonPath("$.transformedPayload.fullName").value("John Doe"))
                .andExpect(jsonPath("$.transformedPayload.riskScore").exists())
                .andExpect(jsonPath("$.transformedPayload.processedAt").exists());
    }

    @Test
    void testMissingApiKey() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        RuleRequest request = new RuleRequest();
        request.setTypeOfRequest("customer_onboard");
        request.setPayload(new HashMap<>());

        mockMvc.perform(post("/rules/consume")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testInvalidApiKey() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        RuleRequest request = new RuleRequest();
        request.setTypeOfRequest("customer_onboard");
        request.setPayload(new HashMap<>());

        mockMvc.perform(post("/rules/consume")
                .header("X-API-KEY", "invalid-key")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testInvalidRequestType() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        RuleRequest request = new RuleRequest();
        request.setTypeOfRequest("unknown_type");
        request.setPayload(new HashMap<>());

        mockMvc.perform(post("/rules/consume")
                .header("X-API-KEY", "change-me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testValidationFailure() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        // Create request with missing required fields
        RuleRequest request = new RuleRequest();
        request.setTypeOfRequest("customer_onboard");
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("firstName", "John");
        // Missing lastName, email, age
        request.setPayload(payload);

        mockMvc.perform(post("/rules/consume")
                .header("X-API-KEY", "change-me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors").isNotEmpty());
    }
}
