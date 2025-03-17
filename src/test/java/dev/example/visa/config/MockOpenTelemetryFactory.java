package dev.example.visa.config;

import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.annotation.Requires;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.ContextPropagators;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * Mock OpenTelemetry factory that provides no-op implementations.
 * This avoids the need for an actual OpenTelemetry backend during tests.
 */
@Slf4j
@Factory
@Requires(env = "test")
public class MockOpenTelemetryFactory {

    /**
     * Provides a no-op OpenTelemetry implementation
     */
    @Singleton
    @Primary
    @Replaces(OpenTelemetry.class)
    public OpenTelemetry openTelemetry() {
        log.info("Creating no-op OpenTelemetry implementation for tests");
        return new NoOpOpenTelemetry();
    }

    /**
     * No-op implementation of OpenTelemetry
     */
    private static class NoOpOpenTelemetry implements OpenTelemetry {
        @Override
        public TracerProvider getTracerProvider() {
            return null;
        }

        @Override
        public Tracer getTracer(String instrumentationName) {
            return new NoOpTracer();
        }

        @Override
        public Tracer getTracer(String instrumentationName, String instrumentationVersion) {
            return new NoOpTracer();
        }

        @Override
        public ContextPropagators getPropagators() {
            return null;
        }
    }

    /**
     * No-op implementation of Tracer
     */
    private static class NoOpTracer implements Tracer {
        @Override
        public SpanBuilder spanBuilder(String spanName) {
            return new NoOpSpanBuilder(spanName);
        }
    }

    /**
     * No-op implementation of SpanBuilder
     */
    private static class NoOpSpanBuilder implements SpanBuilder {
        private final String spanName;

        public NoOpSpanBuilder(String spanName) {
            this.spanName = spanName;
        }

        @Override
        public SpanBuilder setParent(Context context) {
            return this;
        }

        @Override
        public SpanBuilder setNoParent() {
            return this;
        }

        @Override
        public SpanBuilder addLink(SpanContext spanContext) {
            return this;
        }

        @Override
        public SpanBuilder addLink(SpanContext spanContext, Attributes attributes) {
            return this;
        }

        @Override
        public SpanBuilder setAttribute(String key, String value) {
            return this;
        }

        @Override
        public SpanBuilder setAttribute(String key, long value) {
            return this;
        }

        @Override
        public SpanBuilder setAttribute(String key, double value) {
            return this;
        }

        @Override
        public SpanBuilder setAttribute(String key, boolean value) {
            return this;
        }

        @Override
        public <T> SpanBuilder setAttribute(AttributeKey<T> key, T value) {
            return this;
        }

        @Override
        public SpanBuilder setSpanKind(SpanKind spanKind) {
            return this;
        }

        @Override
        public SpanBuilder setStartTimestamp(long startTimestamp, TimeUnit unit) {
            return this;
        }

        @Override
        public Span startSpan() {
            return new NoOpSpan();
        }
    }

    /**
     * No-op implementation of Span
     */
    private static class NoOpSpan implements Span {
        @Override
        public <T> Span setAttribute(AttributeKey<T> key, T value) {
            return this;
        }

        @Override
        public Span setAttribute(String key, String value) {
            return this;
        }

        @Override
        public Span setAttribute(String key, long value) {
            return this;
        }

        @Override
        public Span setAttribute(String key, double value) {
            return this;
        }

        @Override
        public Span setAttribute(String key, boolean value) {
            return this;
        }

        @Override
        public Span addEvent(String name) {
            return this;
        }

        @Override
        public Span addEvent(String name, long timestamp, TimeUnit unit) {
            return this;
        }

        @Override
        public Span addEvent(String name, Attributes attributes) {
            return this;
        }

        @Override
        public Span addEvent(String name, Attributes attributes, long timestamp, TimeUnit unit) {
            return this;
        }

        @Override
        public Span setStatus(StatusCode statusCode) {
            return this;
        }

        @Override
        public Span setStatus(StatusCode statusCode, String description) {
            return this;
        }

        @Override
        public Span recordException(Throwable exception) {
            return this;
        }

        @Override
        public Span recordException(Throwable exception, Attributes additionalAttributes) {
            return this;
        }

        @Override
        public Span updateName(String name) {
            return this;
        }

        @Override
        public void end() {
            // No-op
        }

        @Override
        public void end(long timestamp, TimeUnit unit) {
            // No-op
        }

        @Override
        public SpanContext getSpanContext() {
            return SpanContext.getInvalid();
        }

        @Override
        public boolean isRecording() {
            return false;
        }
    }
}