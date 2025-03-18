package dev.example.visa.config;

import dev.example.visa.client.VisaClickToPayClient;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Primary;
import jakarta.inject.Singleton;
import org.mockito.Mockito;

/**
 * Test configuration factory that provides mock beans for integration testing.
 */
@Factory
public class TestConfiguration {

    /**
     * Creates a mock Visa client for testing.
     * This replaces the actual client implementation to avoid real API calls.
     *
     * @return A mocked VisaClickToPayClient
     */
    @Bean
    @Primary
    @Singleton
    public VisaClickToPayClient mockVisaClickToPayClient() {
        return Mockito.mock(VisaClickToPayClient.class);
    }
}