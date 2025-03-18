package dev.example.visa.config;

import io.micrometer.context.ThreadLocalAccessor;
import org.slf4j.MDC;

import java.util.Collections;
import java.util.Map;

public class MDCThreadLocalAccessor implements ThreadLocalAccessor<Map<String, String>> {
    private static final Object KEY = "MDC";
    private Map<String, String> previousContext = Collections.emptyMap();

    @Override
    public Object key() {
        return KEY;
    }

    @Override
    public Map<String, String> getValue() {
        return MDC.getCopyOfContextMap();
    }

    @Override
    public void setValue(Map<String, String> value) {
        if (value != null) {
            previousContext = MDC.getCopyOfContextMap();
            MDC.setContextMap(value);
        } else {
            MDC.clear();
        }
    }

    @Override
    public void restore(Map<String, String> value) {
        setValue(value);
    }

    @Override
    public void reset() {
        MDC.setContextMap(previousContext != null ? previousContext : Collections.emptyMap());
    }
}