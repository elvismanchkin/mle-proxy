package dev.example.visa.config;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;
import io.opentelemetry.instrumentation.api.instrumenter.util.ClassAndMethod;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.mockito.Mockito;

@Factory
public class TestOpenTelemetryConfig {

    @Bean
    @Singleton
    @Named("micronautCodeTelemetryInstrumenter")
    public Instrumenter<ClassAndMethod, Object> instrumenter() {
        return Mockito.mock(Instrumenter.class);
    }
}