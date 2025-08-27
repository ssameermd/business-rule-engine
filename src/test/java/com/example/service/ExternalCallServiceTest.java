package com.example.service;

import com.example.model.rule.ExternalCall;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExternalCallServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private SpelEvaluator spelEvaluator;

    @Mock
    private ObjectMapper objectMapper;

    private ExternalCallService externalCallService;

    @BeforeEach
    void setUp() {
        externalCallService = new ExternalCallService(restTemplate, spelEvaluator, objectMapper);
    }

    @Test
    void testInvokeGetCall() {
        // Given
        ExternalCall call = new ExternalCall();
        call.setMethod("GET");
        call.setUrl("https://api.example.com/test");

        Map<String, Object> context = new HashMap<>();
        context.put("name", "John");

        ResponseEntity<Map<String, Object>> responseEntity = mock(ResponseEntity.class);
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("status", "success");
        responseBody.put("data", "test data");

        when(spelEvaluator.processTemplate("https://api.example.com/test", context)).thenReturn("https://api.example.com/test");
        when(restTemplate.exchange(eq("https://api.example.com/test"), eq(HttpMethod.GET), any(), any(Class.class)))
                .thenReturn(responseEntity);
        when(responseEntity.getBody()).thenReturn(responseBody);
        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);

        // When
        Object result = externalCallService.invoke(call, context);

        // Then
        assertNotNull(result);
        assertEquals(responseBody, result);
        verify(spelEvaluator).processTemplate("https://api.example.com/test", context);
        verify(restTemplate).exchange(eq("https://api.example.com/test"), eq(HttpMethod.GET), any(), any(Class.class));
    }

    @Test
    void testInvokePostCall() {
        // Given
        ExternalCall call = new ExternalCall();
        call.setMethod("POST");
        call.setUrl("https://api.example.com/test");
        call.setBodyTemplate("{ \"name\": \"{{spel: #name}}\" }");

        Map<String, Object> context = new HashMap<>();
        context.put("name", "John");

        ResponseEntity<Map<String, Object>> responseEntity = mock(ResponseEntity.class);
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("status", "success");

        when(spelEvaluator.processTemplate("https://api.example.com/test", context)).thenReturn("https://api.example.com/test");
        when(spelEvaluator.processTemplate("{ \"name\": \"{{spel: #name}}\" }", context)).thenReturn("{ \"name\": \"John\" }");
        when(restTemplate.exchange(eq("https://api.example.com/test"), eq(HttpMethod.POST), any(), any(Class.class)))
                .thenReturn(responseEntity);
        when(responseEntity.getBody()).thenReturn(responseBody);
        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);

        // When
        Object result = externalCallService.invoke(call, context);

        // Then
        assertNotNull(result);
        assertEquals(responseBody, result);
        verify(spelEvaluator).processTemplate("https://api.example.com/test", context);
        verify(spelEvaluator).processTemplate("{ \"name\": \"{{spel: #name}}\" }", context);
        verify(restTemplate).exchange(eq("https://api.example.com/test"), eq(HttpMethod.POST), any(), any(Class.class));
    }

    @Test
    void testInvokePostCallWithHeaders() {
        // Given
        ExternalCall call = new ExternalCall();
        call.setMethod("POST");
        call.setUrl("https://api.example.com/test");
        call.setBodyTemplate("{ \"name\": \"{{spel: #name}}\" }");

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer {{spel: #token}}");
        headers.put("Content-Type", "application/json");
        call.setHeaders(headers);

        Map<String, Object> context = new HashMap<>();
        context.put("name", "John");
        context.put("token", "abc123");

        ResponseEntity<Map<String, Object>> responseEntity = mock(ResponseEntity.class);
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("status", "success");

        when(spelEvaluator.processTemplate("https://api.example.com/test", context)).thenReturn("https://api.example.com/test");
        when(spelEvaluator.processTemplate("{ \"name\": \"{{spel: #name}}\" }", context)).thenReturn("{ \"name\": \"John\" }");
        when(spelEvaluator.processTemplate("Bearer {{spel: #token}}", context)).thenReturn("Bearer abc123");
        when(spelEvaluator.processTemplate("application/json", context)).thenReturn("application/json");
        when(restTemplate.exchange(eq("https://api.example.com/test"), eq(HttpMethod.POST), any(), any(Class.class)))
                .thenReturn(responseEntity);
        when(responseEntity.getBody()).thenReturn(responseBody);
        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);

        // When
        Object result = externalCallService.invoke(call, context);

        // Then
        assertNotNull(result);
        assertEquals(responseBody, result);
        verify(spelEvaluator).processTemplate("Bearer {{spel: #token}}", context);
        verify(spelEvaluator).processTemplate("application/json", context);
    }

    @Test
    void testInvokeUnsupportedMethod() {
        // Given
        ExternalCall call = new ExternalCall();
        call.setMethod("PUT");
        call.setUrl("https://api.example.com/test");

        Map<String, Object> context = new HashMap<>();

        // When
        Object result = externalCallService.invoke(call, context);

        // Then
        assertNull(result);
    }

    @Test
    void testInvokeWithException() {
        // Given
        ExternalCall call = new ExternalCall();
        call.setMethod("GET");
        call.setUrl("https://api.example.com/test");

        Map<String, Object> context = new HashMap<>();

        when(spelEvaluator.processTemplate("https://api.example.com/test", context))
                .thenThrow(new RuntimeException("Network error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> externalCallService.invoke(call, context));
    }

    @Test
    void testInvokeGetCallWithNullBody() {
        // Given
        ExternalCall call = new ExternalCall();
        call.setMethod("GET");
        call.setUrl("https://api.example.com/test");

        Map<String, Object> context = new HashMap<>();

        ResponseEntity<Map<String, Object>> responseEntity = mock(ResponseEntity.class);

        when(spelEvaluator.processTemplate("https://api.example.com/test", context)).thenReturn("https://api.example.com/test");
        when(restTemplate.exchange(eq("https://api.example.com/test"), eq(HttpMethod.GET), any(), any(Class.class)))
                .thenReturn(responseEntity);
        when(responseEntity.getBody()).thenReturn(null);
        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.NO_CONTENT);

        // When
        Object result = externalCallService.invoke(call, context);

        // Then
        assertNull(result);
    }

    @Test
    void testInvokePostCallWithNullBodyTemplate() {
        // Given
        ExternalCall call = new ExternalCall();
        call.setMethod("POST");
        call.setUrl("https://api.example.com/test");
        // bodyTemplate is null

        Map<String, Object> context = new HashMap<>();

        ResponseEntity<Map<String, Object>> responseEntity = mock(ResponseEntity.class);
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("status", "success");

        when(spelEvaluator.processTemplate("https://api.example.com/test", context)).thenReturn("https://api.example.com/test");
        when(restTemplate.exchange(eq("https://api.example.com/test"), eq(HttpMethod.POST), any(), any(Class.class)))
                .thenReturn(responseEntity);
        when(responseEntity.getBody()).thenReturn(responseBody);
        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);

        // When
        Object result = externalCallService.invoke(call, context);

        // Then
        assertNotNull(result);
        assertEquals(responseBody, result);
        verify(spelEvaluator, never()).processTemplate(isNull(), any());
    }
}
