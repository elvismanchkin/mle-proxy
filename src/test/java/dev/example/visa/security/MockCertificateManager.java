package dev.example.visa.security;

import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.ShutdownEvent;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

/**
 * Mock implementation of CertificateManager that does nothing in test environment.
 */
@Slf4j
@Singleton
@Primary
@Requires(env = "test")
@Replaces(CertificateManager.class)
public class MockCertificateManager extends CertificateManager {

    public MockCertificateManager() {
        super(null);
    }

    @Override
    public void initialize() {
        log.info("Mock certificate manager initialized - no action taken");
    }

    @Override
    public Publisher<Boolean> loadCertificatesFromVault() {
        log.info("Mock certificate loading - returning success without doing anything");
        return Mono.just(true);
    }

    @Override
    public boolean supports(ShutdownEvent event) {
        return super.supports(event);
    }
}