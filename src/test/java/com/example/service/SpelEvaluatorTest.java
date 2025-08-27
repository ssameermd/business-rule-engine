package com.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SpelEvaluatorTest {

    @Mock
    private ObjectMapper objectMapper;

    private SpelEvaluator spelEvaluator;

    @BeforeEach
    void setUp() {
        spelEvaluator = new SpelEvaluator(objectMapper);
    }

    @Test
    void testEvaluateSimpleExpression() {
        Map<String, Object> context = new HashMap<>();
        context.put("x", 10);
        context.put("y", 5);

        Object result = spelEvaluator.evaluate("#x + #y", context);
        assertEquals(15, result);
    }

    @Test
    void testEvaluateBooleanExpression() {
        Map<String, Object> context = new HashMap<>();
        context.put("age", 25);
        context.put("threshold", 18);

        boolean result = spelEvaluator.evaluateBoolean("#age >= #threshold", context);
        assertTrue(result);
    }

    @Test
    void testEvaluateNullExpression() {
        Map<String, Object> context = new HashMap<>();
        Object result = spelEvaluator.evaluate(null, context);
        assertNull(result);
    }

    @Test
    void testEvaluateEmptyExpression() {
        Map<String, Object> context = new HashMap<>();
        Object result = spelEvaluator.evaluate("   ", context);
        assertNull(result);
    }

    @Test
    void testEvaluateBooleanWithNonBooleanResult() {
        Map<String, Object> context = new HashMap<>();
        context.put("value", "true");

        boolean result = spelEvaluator.evaluateBoolean("#value", context);
        assertTrue(result);
    }

    @Test
    void testProcessTemplate() {
        Map<String, Object> context = new HashMap<>();
        context.put("name", "John");
        context.put("age", 30);

        String template = "Hello {{spel: #name}}, you are {{spel: #age}} years old";
        String result = spelEvaluator.processTemplate(template, context);

        assertEquals("Hello John, you are 30 years old", result);
    }

    @Test
    void testProcessTemplateWithNullTemplate() {
        Map<String, Object> context = new HashMap<>();
        String result = spelEvaluator.processTemplate(null, context);
        assertNull(result);
    }

    @Test
    void testProcessTemplateWithNoExpressions() {
        Map<String, Object> context = new HashMap<>();
        String template = "Hello World";
        String result = spelEvaluator.processTemplate(template, context);

        assertEquals("Hello World", result);
    }

    @Test
    void testCreateContext() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", "John");

        Map<String, Object> ctx = new HashMap<>();
        ctx.put("temp", "value");

        Map<String, Object> defaults = new HashMap<>();
        defaults.put("country", "US");

        Map<String, String> env = new HashMap<>();
        env.put("ENV", "test");

        Map<String, Object> context = spelEvaluator.createContext(payload, ctx, defaults, env);

        assertEquals(payload, context.get("payload"));
        assertEquals(ctx, context.get("ctx"));
        assertEquals(defaults, context.get("defaults"));
        assertEquals(env, context.get("env"));
        assertNotNull(context.get("now"));
        assertTrue(context.get("now") instanceof LocalDateTime);
    }

    @Test
    void testMapAccessWithBrackets() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("firstName", "John");
        payload.put("lastName", "Doe");

        Map<String, Object> context = new HashMap<>();
        context.put("payload", payload);

        Object result = spelEvaluator.evaluate("#payload['firstName']", context);
        assertEquals("John", result);
    }

    @Test
    void testMapAccessWithDot() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("firstName", "John");

        Map<String, Object> context = new HashMap<>();
        context.put("payload", payload);

        Object result = spelEvaluator.evaluate("#payload.firstName", context);
        assertEquals("John", result);
    }

    @Test
    void testComplexExpression() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("age", 25);
        payload.put("country", "US");

        Map<String, Object> defaults = new HashMap<>();
        defaults.put("riskThreshold", 70);

        Map<String, Object> context = new HashMap<>();
        context.put("payload", payload);
        context.put("defaults", defaults);

        Object result = spelEvaluator.evaluate("#payload['age'] < 30 and #payload['country'] == 'US'", context);
        assertTrue((Boolean) result);
    }

    @Test
    void testStringOperations() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("email", "test@example.com");

        Map<String, Object> context = new HashMap<>();
        context.put("payload", payload);

        Object result = spelEvaluator.evaluate("(#payload['email']).endsWith('.com')", context);
        assertTrue((Boolean) result);
    }

    @Test
    void testArithmeticOperations() {
        Map<String, Object> context = new HashMap<>();
        context.put("x", 10);
        context.put("y", 3);

        Object result = spelEvaluator.evaluate("#x * #y + 5", context);
        assertEquals(35, result);
    }

    @Test
    void testTernaryOperator() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("age", 25);

        Map<String, Object> context = new HashMap<>();
        context.put("payload", payload);

        Object result = spelEvaluator.evaluate("#payload['age'] >= 18 ? 'adult' : 'minor'", context);
        assertEquals("adult", result);
    }

    @Test
    void testElvisOperator() {
        Map<String, Object> payload = new HashMap<>();
        // age is null

        Map<String, Object> context = new HashMap<>();
        context.put("payload", payload);

        Object result = spelEvaluator.evaluate("#payload['age'] ?: 0", context);
        assertEquals(0, result);
    }

    @Test
    void testNullSafeMapAccess() {
        // Given
        Map<String, Object> ctx = new HashMap<>();
        // i18nResult is null
        ctx.put("i18nResult", null);

        Map<String, Object> context = new HashMap<>();
        context.put("ctx", ctx);

        // When
        Object result = spelEvaluator.evaluate("#ctx['i18nResult'] != null ? #ctx['i18nResult']['ALLOCATION']['AllocatetheApprovers'] : 'Default Allocation Message'", context);

        // Then
        assertEquals("Default Allocation Message", result);
    }

    @Test
    void testNullSafeMapAccessWithValidData() {
        // Given
        Map<String, Object> allocation = new HashMap<>();
        allocation.put("AllocatetheApprovers", "Custom Allocation Message");

        Map<String, Object> i18nResult = new HashMap<>();
        i18nResult.put("ALLOCATION", allocation);

        Map<String, Object> ctx = new HashMap<>();
        ctx.put("i18nResult", i18nResult);

        Map<String, Object> context = new HashMap<>();
        context.put("ctx", ctx);

        // When
        Object result = spelEvaluator.evaluate("#ctx['i18nResult'] != null ? #ctx['i18nResult']['ALLOCATION']['AllocatetheApprovers'] : 'Default Allocation Message'", context);

        // Then
        assertEquals("Custom Allocation Message", result);
    }

    @Test
    void testNullSafeStringConcatenation() {
        // Given
        Map<String, Object> payload = new HashMap<>();
        payload.put("firstName", "John");
        payload.put("lastName", null);

        Map<String, Object> context = new HashMap<>();
        context.put("payload", payload);

        // When
        Object result = spelEvaluator.evaluate("(#payload['firstName'] != null ? #payload['firstName'] : '') + ' ' + (#payload['lastName'] != null ? #payload['lastName'] : '')", context);

        // Then
        assertEquals("John ", result);
    }

    @Test
    void testNullSafeEmailValidation() {
        // Given
        Map<String, Object> payload = new HashMap<>();
        payload.put("email", "test@example.corp");

        Map<String, Object> context = new HashMap<>();
        context.put("payload", payload);

        // When
        Object result = spelEvaluator.evaluate("(#payload['email'] != null and (#payload['email']).endsWith('.corp')) or (#payload['email'] != null and (#payload['email']).endsWith('.biz'))", context);

        // Then
        assertTrue((Boolean) result);
    }

    @Test
    void testNullSafeEmailValidationWithNullEmail() {
        // Given
        Map<String, Object> payload = new HashMap<>();
        payload.put("email", null);

        Map<String, Object> context = new HashMap<>();
        context.put("payload", payload);

        // When
        Object result = spelEvaluator.evaluate("(#payload['email'] != null and (#payload['email']).endsWith('.corp')) or (#payload['email'] != null and (#payload['email']).endsWith('.biz'))", context);

        // Then
        assertFalse((Boolean) result);
    }
}
