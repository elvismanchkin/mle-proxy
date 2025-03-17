package dev.example.visa.config;

import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.annotation.Requires;
import io.micronaut.tracing.opentelemetry.DefaultOpenTelemetryFactory;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import jakarta.inject.Singleton;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Mock OpenTelemetry configuration for testing.
 */
@Singleton
@Primary
@Requires(env = "test")
@Replaces(DefaultOpenTelemetryFactory.class)
public class MockOpenTelemetryFactory {

    @Singleton
    public OpenTelemetry openTelemetry() {
        OpenTelemetry mockTelemetry = mock(OpenTelemetry.class);
        Tracer mockTracer = mock(Tracer.class);

        when(mockTelemetry.getTracer(org.mockito.ArgumentMatchers.anyString())).thenReturn(mockTracer);

        return mockTelemetry;
    }
}