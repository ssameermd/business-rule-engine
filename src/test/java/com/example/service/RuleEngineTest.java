package com.example.service;

import com.example.model.RuleRequest;
import com.example.model.RuleResponse;
import com.example.model.rule.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RuleEngineTest {

    @Mock
    private SpelEvaluator spelEvaluator;

    @Mock
    private ExternalCallService externalCallService;

    @Mock
    private ObjectMapper objectMapper;

    private RuleEngine ruleEngine;

    @BeforeEach
    void setUp() {
        ruleEngine = new RuleEngine(spelEvaluator, externalCallService, objectMapper);
    }

    @Test
    void testExecuteWithSimpleValidationRule() {
        // Given
        RuleRequest request = new RuleRequest();
        request.setTypeOfRequest("test");
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", "John");
        payload.put("age", 25);
        request.setPayload(payload);

        RuleConfig config = new RuleConfig();
        config.setType("test");
        config.setDefaults(new HashMap<>());

        List<ValidationRule> validations = Arrays.asList(
            new ValidationRule("$.name", true, "string", null, null, "Name is required"),
            new ValidationRule("$.age", true, "number", null, "#payload['age'] >= 18", "Must be 18+")
        );

        Rule rule = new Rule();
        rule.setId("v1");
        rule.setDescription("Validation test");
        rule.setValidate(validations);
        rule.setStopOnValidationError(true);

        config.setRules(Arrays.asList(rule));

        when(objectMapper.convertValue(any(), eq(Map.class))).thenReturn(payload);
        when(spelEvaluator.createContext(any(), any(), any(), any())).thenReturn(new HashMap<>());
        when(spelEvaluator.evaluateBoolean(anyString(), any())).thenReturn(true);

        // When
        RuleResponse response = ruleEngine.execute(request, config);

        // Then
        assertNotNull(response);
        assertTrue(response.isValid());
        assertEquals("test", response.getTypeOfRequest());
        assertTrue(response.getErrors().isEmpty());
        assertNotNull(response.getTrace());
        assertEquals(1, response.getTrace().size());
        assertEquals("SUCCESS", response.getTrace().get(0).get("status"));
    }

    @Test
    void testExecuteWithValidationFailure() {
        // Given
        RuleRequest request = new RuleRequest();
        request.setTypeOfRequest("test");
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", "John");
        // age is missing
        request.setPayload(payload);

        RuleConfig config = new RuleConfig();
        config.setType("test");
        config.setDefaults(new HashMap<>());

        List<ValidationRule> validations = Arrays.asList(
            new ValidationRule("$.age", true, "number", null, null, "Age is required")
        );

        Rule rule = new Rule();
        rule.setId("v1");
        rule.setDescription("Validation test");
        rule.setValidate(validations);
        rule.setStopOnValidationError(true);

        config.setRules(Arrays.asList(rule));

        when(objectMapper.convertValue(any(), eq(Map.class))).thenReturn(payload);
        when(spelEvaluator.createContext(any(), any(), any(), any())).thenReturn(new HashMap<>());

        // When
        RuleResponse response = ruleEngine.execute(request, config);

        // Then
        assertNotNull(response);
        assertFalse(response.isValid());
        assertEquals(1, response.getErrors().size());
        assertTrue(response.getErrors().get(0).contains("Age is required"));
        assertEquals("FAILED", response.getTrace().get(0).get("status"));
    }

    @Test
    void testExecuteWithTransformRule() {
        // Given
        RuleRequest request = new RuleRequest();
        request.setTypeOfRequest("test");
        Map<String, Object> payload = new HashMap<>();
        payload.put("firstName", "John");
        payload.put("lastName", "Doe");
        request.setPayload(payload);

        RuleConfig config = new RuleConfig();
        config.setType("test");
        config.setDefaults(new HashMap<>());

        List<TransformStep> transforms = Arrays.asList(
            new TransformStep("SPEL", "#ctx['fullName'] = #payload['firstName'] + ' ' + #payload['lastName']")
        );

        Rule rule = new Rule();
        rule.setId("t1");
        rule.setDescription("Transform test");
        rule.setTransform(transforms);

        config.setRules(Arrays.asList(rule));

        when(objectMapper.convertValue(any(), eq(Map.class))).thenReturn(payload);
        when(spelEvaluator.createContext(any(), any(), any(), any())).thenReturn(new HashMap<>());
        when(spelEvaluator.evaluate(anyString(), any())).thenReturn("John Doe");

        // When
        RuleResponse response = ruleEngine.execute(request, config);

        // Then
        assertNotNull(response);
        assertTrue(response.isValid());
        assertEquals("TRANSFORMED", response.getTrace().get(0).get("status"));
        verify(spelEvaluator).evaluate("#ctx['fullName'] = #payload['firstName'] + ' ' + #payload['lastName']", any());
    }

    @Test
    void testExecuteWithWhenCondition() {
        // Given
        RuleRequest request = new RuleRequest();
        request.setTypeOfRequest("test");
        Map<String, Object> payload = new HashMap<>();
        payload.put("age", 25);
        request.setPayload(payload);

        RuleConfig config = new RuleConfig();
        config.setType("test");
        config.setDefaults(new HashMap<>());

        Rule rule = new Rule();
        rule.setId("w1");
        rule.setDescription("When condition test");
        rule.setWhen("#payload['age'] >= 18");

        config.setRules(Arrays.asList(rule));

        when(objectMapper.convertValue(any(), eq(Map.class))).thenReturn(payload);
        when(spelEvaluator.createContext(any(), any(), any(), any())).thenReturn(new HashMap<>());
        when(spelEvaluator.evaluateBoolean("#payload['age'] >= 18", any())).thenReturn(true);

        // When
        RuleResponse response = ruleEngine.execute(request, config);

        // Then
        assertNotNull(response);
        assertTrue(response.isValid());
        assertEquals("SUCCESS", response.getTrace().get(0).get("status"));
    }

    @Test
    void testExecuteWithWhenConditionFalse() {
        // Given
        RuleRequest request = new RuleRequest();
        request.setTypeOfRequest("test");
        Map<String, Object> payload = new HashMap<>();
        payload.put("age", 16);
        request.setPayload(payload);

        RuleConfig config = new RuleConfig();
        config.setType("test");
        config.setDefaults(new HashMap<>());

        Rule rule = new Rule();
        rule.setId("w1");
        rule.setDescription("When condition test");
        rule.setWhen("#payload['age'] >= 18");

        config.setRules(Arrays.asList(rule));

        when(objectMapper.convertValue(any(), eq(Map.class))).thenReturn(payload);
        when(spelEvaluator.createContext(any(), any(), any(), any())).thenReturn(new HashMap<>());
        when(spelEvaluator.evaluateBoolean("#payload['age'] >= 18", any())).thenReturn(false);

        // When
        RuleResponse response = ruleEngine.execute(request, config);

        // Then
        assertNotNull(response);
        assertTrue(response.isValid());
        assertEquals("SKIPPED", response.getTrace().get(0).get("status"));
        assertEquals("when condition not met", response.getTrace().get(0).get("reason"));
    }

    @Test
    void testExecuteWithExternalCall() {
        // Given
        RuleRequest request = new RuleRequest();
        request.setTypeOfRequest("test");
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", "John");
        request.setPayload(payload);

        RuleConfig config = new RuleConfig();
        config.setType("test");
        config.setDefaults(new HashMap<>());

        ExternalCall externalCall = new ExternalCall();
        externalCall.setMethod("GET");
        externalCall.setUrl("https://api.example.com/test");
        externalCall.setSaveAs("apiResult");

        Rule rule = new Rule();
        rule.setId("x1");
        rule.setDescription("External call test");
        rule.setExternalCall(externalCall);

        config.setRules(Arrays.asList(rule));

        Map<String, Object> apiResult = new HashMap<>();
        apiResult.put("status", "success");

        when(objectMapper.convertValue(any(), eq(Map.class))).thenReturn(payload);
        when(spelEvaluator.createContext(any(), any(), any(), any())).thenReturn(new HashMap<>());
        when(externalCallService.invoke(any(), any())).thenReturn(apiResult);

        // When
        RuleResponse response = ruleEngine.execute(request, config);

        // Then
        assertNotNull(response);
        assertTrue(response.isValid());
        assertEquals("EXTERNAL_CALL", response.getTrace().get(0).get("status"));
        assertEquals(1, response.getExternalCalls().size());
        assertEquals("https://api.example.com/test", response.getExternalCalls().get(0).get("url"));
        assertEquals("GET", response.getExternalCalls().get(0).get("method"));
    }

    @Test
    void testExecuteWithStopAction() {
        // Given
        RuleRequest request = new RuleRequest();
        request.setTypeOfRequest("test");
        Map<String, Object> payload = new HashMap<>();
        request.setPayload(payload);

        RuleConfig config = new RuleConfig();
        config.setType("test");
        config.setDefaults(new HashMap<>());

        Action stopAction = new Action();
        stopAction.setAction("STOP");

        Rule rule1 = new Rule();
        rule1.setId("r1");
        rule1.setDescription("First rule");
        rule1.setOnSuccess(stopAction);

        Rule rule2 = new Rule();
        rule2.setId("r2");
        rule2.setDescription("Second rule");

        config.setRules(Arrays.asList(rule1, rule2));

        when(objectMapper.convertValue(any(), eq(Map.class))).thenReturn(payload);
        when(spelEvaluator.createContext(any(), any(), any(), any())).thenReturn(new HashMap<>());

        // When
        RuleResponse response = ruleEngine.execute(request, config);

        // Then
        assertNotNull(response);
        assertTrue(response.isValid());
        assertEquals(1, response.getTrace().size());
        assertEquals("STOPPED", response.getTrace().get(0).get("status"));
    }

    @Test
    void testExecuteWithException() {
        // Given
        RuleRequest request = new RuleRequest();
        request.setTypeOfRequest("test");
        Map<String, Object> payload = new HashMap<>();
        request.setPayload(payload);

        RuleConfig config = new RuleConfig();
        config.setType("test");
        config.setDefaults(new HashMap<>());

        Rule rule = new Rule();
        rule.setId("e1");
        rule.setDescription("Error rule");

        config.setRules(Arrays.asList(rule));

        when(objectMapper.convertValue(any(), eq(Map.class))).thenReturn(payload);
        when(spelEvaluator.createContext(any(), any(), any(), any())).thenThrow(new RuntimeException("Test error"));

        // When
        RuleResponse response = ruleEngine.execute(request, config);

        // Then
        assertNotNull(response);
        assertFalse(response.isValid());
        assertEquals(1, response.getErrors().size());
        assertTrue(response.getErrors().get(0).contains("Rule e1 failed"));
        assertEquals("ERROR", response.getTrace().get(0).get("status"));
    }

    @Test
    void testGetValueByPath() {
        // Given
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", "John");
        payload.put("age", 25);

        // When & Then
        assertEquals("John", ruleEngine.getValueByPath(payload, "$.name"));
        assertEquals(25, ruleEngine.getValueByPath(payload, "$.age"));
        assertEquals(payload, ruleEngine.getValueByPath(payload, null));
        assertEquals(payload, ruleEngine.getValueByPath(payload, ""));
    }

    @Test
    void testIsValidType() {
        // Given & When & Then
        assertTrue(ruleEngine.isValidType("test", "string"));
        assertTrue(ruleEngine.isValidType(123, "number"));
        assertTrue(ruleEngine.isValidType(true, "boolean"));
        assertTrue(ruleEngine.isValidType(new HashMap<>(), "object"));
        assertTrue(ruleEngine.isValidType(new ArrayList<>(), "array"));
        assertTrue(ruleEngine.isValidType("test", "unknown"));
    }
}
