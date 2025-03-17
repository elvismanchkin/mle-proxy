package dev.example.visa.security;

import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Mock implementation of VaultService for testing.
 * Returns dummy values instead of interacting with Vault.
 */
@Slf4j
@Singleton
@Primary
@Requires(env = "test")
@Replaces(VaultService.class)
public class MockVaultService extends VaultService {

    public MockVaultService() {
//        super(null); // We don't need any dependencies for the mock
    }

    @Override
    public Mono<String> getClientCertificate() {
        return Mono.just("-----BEGIN CERTIFICATE-----\nMOCK_CERTIFICATE\n-----END CERTIFICATE-----");
    }

    @Override
    public Mono<String> getClientKey() {
        return Mono.just("-----BEGIN PRIVATE KEY-----\nMOCK_PRIVATE_KEY\n-----END PRIVATE KEY-----");
    }

    @Override
    public Mono<String> getServerCertificate() {
        return Mono.just("-----BEGIN CERTIFICATE-----\nMOCK_SERVER_CERTIFICATE\n-----END CERTIFICATE-----");
    }

    @Override
    public Mono<String> getApiKey() {
        return Mono.just("mock-api-key");
    }

    @Override
    public Mono<String> getApiSecret() {
        return Mono.just("mock-api-secret");
    }
}