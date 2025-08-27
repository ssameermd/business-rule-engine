package com.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.Expression;
import org.springframework.expression.TypedValue;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.AccessException;
import org.springframework.expression.PropertyAccessor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SpelEvaluator {

    private static final Logger logger = LoggerFactory.getLogger(SpelEvaluator.class);
    private static final Pattern TEMPLATE_PATTERN = Pattern.compile("\\{\\{spel:\\s*(.*?)\\s*\\}\\}");
    private final ObjectMapper objectMapper;
    private final SpelExpressionParser parser = new SpelExpressionParser();

    public SpelEvaluator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Object evaluate(String expression, Map<String, Object> vars) {
        try {
            if (expression == null || expression.trim().isEmpty()) {
                return null;
            }
            StandardEvaluationContext context = createEvaluationContext(vars);
            Expression exp = parser.parseExpression(expression);
            return exp.getValue(context);
        } catch (Exception e) {
            logger.error("Error evaluating expression: {}", expression, e);
            throw new RuntimeException("Expression evaluation failed: " + e.getMessage(), e);
        }
    }

    public boolean evaluateBoolean(String expression, Map<String, Object> vars) {
        Object result = evaluate(expression, vars);
        if (result instanceof Boolean) {
            return (Boolean) result;
        }
        return result != null && Boolean.parseBoolean(String.valueOf(result));
    }

    public String processTemplate(String template, Map<String, Object> context) {
        if (template == null) {
            return null;
        }
        Matcher matcher = TEMPLATE_PATTERN.matcher(template);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String expr = matcher.group(1).trim();
            Object evaluated = evaluate(expr, context);
            String replacement = evaluated != null ? evaluated.toString() : "";
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    public Map<String, Object> createContext(Object payload, Map<String, Object> ctx,
                                             Map<String, Object> defaults,
                                             Map<String, String> env) {
        Map<String, Object> context = new HashMap<>();
        context.put("payload", payload);
        context.put("ctx", ctx);
        context.put("defaults", defaults);
        context.put("env", env);
        context.put("now", java.time.LocalDateTime.now());
        return context;
    }

    private StandardEvaluationContext createEvaluationContext(Map<String, Object> vars) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        for (Map.Entry<String, Object> e : vars.entrySet()) {
            context.setVariable(e.getKey(), e.getValue());
        }
        context.addPropertyAccessor(new MapLikeAccessor());
        return context;
    }

    static class MapLikeAccessor implements PropertyAccessor {
        @Override
        public Class<?>[] getSpecificTargetClasses() { return new Class[] { Map.class }; }
        @Override
        public boolean canRead(EvaluationContext context, Object target, String name) { return ((Map<?, ?>) target).containsKey(name); }
        @Override
        public TypedValue read(EvaluationContext context, Object target, String name) throws AccessException {
            return new TypedValue(((Map<?, ?>) target).get(name));
        }
        @Override
        public boolean canWrite(EvaluationContext context, Object target, String name) { return true; }
        @Override
        @SuppressWarnings("unchecked")
        public void write(EvaluationContext context, Object target, String name, Object newValue) throws AccessException {
            ((Map<String, Object>) target).put(name, newValue);
        }
    }
}

