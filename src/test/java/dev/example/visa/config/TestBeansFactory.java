package dev.example.visa.config;

import dev.example.visa.client.VisaClickToPayClient;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Replaces;
import jakarta.inject.Singleton;

import static org.mockito.Mockito.mock;

/**
 * Factory for creating mock beans used in tests.
 */
@Factory
public class TestBeansFactory {

    /**
     * Creates a mock VisaClickToPayClient for testing.
     * This avoids the need for actual HTTP calls.
     */
    @Singleton
    @Replaces(VisaClickToPayClient.class)
    public VisaClickToPayClient mockVisaClient() {
        return mock(VisaClickToPayClient.class);
    }
}