package dev.example.visa.config;

import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import io.micronaut.http.client.HttpClientConfiguration;
import io.micronaut.http.ssl.ClientSslConfiguration;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

/**
 * Central configuration for unit tests.
 * This class consolidates all test-specific configurations in one place.
 * When the "test" environment is active, it provides mock implementations
 * to disable external dependencies.
 */
@Slf4j
@Factory
@Requires(env = "test")
public class TestConfiguration {

    /**
     * Disables SSL verification for HTTP clients in test environment
     */
    @Singleton
    @Requires(env = "test")
    public static class HttpClientConfigurationOverride implements BeanCreatedEventListener<HttpClientConfiguration> {
        @Override
        public HttpClientConfiguration onCreated(BeanCreatedEvent<HttpClientConfiguration> event) {
            HttpClientConfiguration configuration = event.getBean();
            ClientSslConfiguration sslConfiguration = new ClientSslConfiguration();
            sslConfiguration.setInsecureTrustAllCertificates(true);
            configuration.setSslConfiguration(sslConfiguration);
            return configuration;
        }
    }
}