package dev.example.visa.test;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.TestInstance;

/**
 * Base class for unit tests.
 * Configures the test environment with mocked external dependencies.
 */
@MicronautTest(
        environments = {"test"},
        startApplication = false,
        transactional = false
)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseUnitTest {
    // Common test utilities can be added here
}