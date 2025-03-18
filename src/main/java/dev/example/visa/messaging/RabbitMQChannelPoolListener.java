package dev.example.visa.messaging;

import com.rabbitmq.client.Channel;
import io.micronaut.context.annotation.Value;
import io.micronaut.rabbitmq.connect.ChannelInitializer;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Initializes RabbitMQ channels when they are created from the pool.
 * Sets up the exchanges and queues needed for the application.
 */
@Slf4j
@Singleton
public class RabbitMQChannelPoolListener extends ChannelInitializer {

    @Value("${rabbitmq.rpc.request-queue:visa-click-to-pay-requests}")
    private String requestQueue;

    @Value("${rabbitmq.exchange.name:visa-click-to-pay-exchange}")
    private String exchangeName;

    @Override
    public void initialize(Channel channel, String name) throws IOException {
        log.info("Initializing RabbitMQ channel: {}", name);

        try {
            // Declare the direct exchange
            channel.exchangeDeclare(exchangeName, "direct", true);
            log.info("Declared exchange: {}", exchangeName);

            // Define operation queues
            Map<String, String> operationQueues = new HashMap<>();
            operationQueues.put("enrollData", requestQueue + ".enrollData");
            operationQueues.put("enrollPaymentInstruments", requestQueue + ".enrollPaymentInstruments");
            operationQueues.put("requestStatus", requestQueue + ".requestStatus");
            operationQueues.put("managePaymentInstruments", requestQueue + ".managePaymentInstruments");
            operationQueues.put("manageConsumerInformation", requestQueue + ".manageConsumerInformation");
            operationQueues.put("deleteConsumerInformation", requestQueue + ".deleteConsumerInformation");
            operationQueues.put("deletePaymentInstruments", requestQueue + ".deletePaymentInstruments");
            operationQueues.put("getData", requestQueue + ".getData");

            // Create queues and bind them to exchange
            for (Map.Entry<String, String> entry : operationQueues.entrySet()) {
                String routingKey = entry.getKey();
                String queueName = entry.getValue();

                // Declare queue with durable, non-exclusive, non-auto-delete
                channel.queueDeclare(queueName, true, false, false, null);

                // Bind queue to exchange with routing key
                channel.queueBind(queueName, exchangeName, routingKey);

                log.info("Declared and bound queue: {} with routing key: {}", queueName, routingKey);
            }
        } catch (IOException e) {
            log.error("Failed to initialize RabbitMQ channel: {}", name, e);
            throw e;
        }
    }
}