package dev.example.visa.messaging;

import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.rabbitmq.connect.RabbitConnectionFactoryConfig;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Set;

/**
 * TestContainers RabbitMQ implementation for integration testing.
 * This class is only active when the 'integration-test' environment is enabled.
 */
@Slf4j
@Factory
@Requires(env = "integration-test")
@Requires(classes = RabbitMQContainer.class)
public class RabbitMQTestContainer {

    private RabbitMQContainer rabbitContainer;

    @PostConstruct
    public void setup() {
        log.info("Initializing RabbitMQ test container");
        rabbitContainer = new RabbitMQContainer(DockerImageName.parse("rabbitmq:3.12-management"))
                .withExposedPorts(5672, 15672)
                .withAdminPassword("admin")
                .withAdminUser("admin")
                .withUser("guest", "guest", Set.of("administrator"));

        rabbitContainer.start();

        // Set system properties for Micronaut to pick up
        System.setProperty("rabbitmq.host", rabbitContainer.getHost());
        System.setProperty("rabbitmq.port", String.valueOf(rabbitContainer.getAmqpPort()));
        System.setProperty("rabbitmq.uri", rabbitContainer.getAmqpUrl());
        System.setProperty("rabbitmq.username", "guest");
        System.setProperty("rabbitmq.password", "guest");

        log.info("RabbitMQ test container started on {}:{}",
                rabbitContainer.getHost(), rabbitContainer.getAmqpPort());
    }

    @PreDestroy
    public void cleanup() {
        if (rabbitContainer != null && rabbitContainer.isRunning()) {
            log.info("Stopping RabbitMQ test container");
            rabbitContainer.stop();
        }
    }

    /**
     * Provides a custom RabbitMQ connection factory config for the test container
     */
    @Singleton
    @Replaces(RabbitConnectionFactoryConfig.class)
    @Requires(env = "integration-test")
    @Requires(beans = RabbitMQContainer.class)
    public RabbitConnectionFactoryConfig rabbitConfig() {
        return new RabbitConnectionFactoryConfig("default") {
            @Override
            @NonNull
            public String getHost() {
                return rabbitContainer.getHost();
            }

            @Override
            public int getPort() {
                return rabbitContainer.getAmqpPort();
            }

            @Override
            @NonNull
            public String getUsername() {
                return "guest";
            }

            @Override
            @NonNull
            public String getPassword() {
                return "guest";
            }
        };
    }
}