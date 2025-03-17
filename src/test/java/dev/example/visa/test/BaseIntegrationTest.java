package dev.example.visa.test;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.TestInstance;

/**
 * Base class for integration tests.
 * Uses TestContainers for RabbitMQ while mocking other external dependencies.
 */
@MicronautTest(
        environments = {"integration-test"},
        startApplication = true,
        transactional = false
)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseIntegrationTest {
    // Common integration test utilities can be added here
}