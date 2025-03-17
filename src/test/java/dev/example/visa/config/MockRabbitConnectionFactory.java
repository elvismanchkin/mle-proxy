package dev.example.visa.config;

import com.rabbitmq.client.Connection;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.annotation.Requires;
import io.micronaut.rabbitmq.connect.RabbitConnectionFactory;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Mock RabbitMQ Connection Factory for testing.
 * Prevents NPE during shutdown.
 */
@Slf4j
@Singleton
@Primary
@Requires(env = "test")
@Replaces(RabbitConnectionFactory.class)
public class MockRabbitConnectionFactory extends RabbitConnectionFactory {

    public MockRabbitConnectionFactory() {
        super();
    }

    public Connection getConnection() {
        return mock(Connection.class);
    }

    public void close() {
        log.info("Mock RabbitMQ connection factory closed");
    }

    public ExecutorService getExecutorService() {
        ExecutorService mockExecutor = mock(ExecutorService.class);
        when(mockExecutor.shutdownNow()).thenReturn(null);
        return mockExecutor;
    }
}