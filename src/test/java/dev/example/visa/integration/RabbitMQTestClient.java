package dev.example.visa.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.GetResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A utility client for testing RabbitMQ message handling.
 */
public class RabbitMQTestClient implements Closeable {
    private static final Logger LOG = LoggerFactory.getLogger(RabbitMQTestClient.class);

    private final Connection connection;
    private final Channel channel;
    private final ObjectMapper objectMapper;

    /**
     * Creates a new RabbitMQ test client.
     *
     * @param amqpUrl The AMQP URL to connect to
     * @throws IOException If connection fails
     * @throws TimeoutException If connection times out
     */
    public RabbitMQTestClient(String amqpUrl) throws IOException, TimeoutException, URISyntaxException, NoSuchAlgorithmException, KeyManagementException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUri(amqpUrl);

        this.connection = factory.newConnection();
        this.channel = connection.createChannel();
        this.objectMapper = new ObjectMapper();

        // Set up the test exchange
        channel.exchangeDeclare("visa-click-to-pay-exchange", "direct", true);
    }

    /**
     * Declares a temporary queue.
     *
     * @return The queue name
     * @throws IOException If queue declaration fails
     */
    public String declareQueue() throws IOException {
        String queueName = "test-queue-" + UUID.randomUUID();
        channel.queueDeclare(queueName, false, true, true, null);
        return queueName;
    }

    /**
     * Declares a temporary queue with a message TTL.
     *
     * @param ttl Time-to-live in milliseconds
     * @return The queue name
     * @throws IOException If queue declaration fails
     */
    public String declareQueue(int ttl) throws IOException {
        String queueName = "test-queue-" + UUID.randomUUID();

        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-message-ttl", ttl);

        channel.queueDeclare(queueName, false, true, true, arguments);
        return queueName;
    }

    /**
     * Sends a request via RabbitMQ.
     *
     * @param exchange The exchange to publish to
     * @param routingKey The routing key
     * @param correlationId The correlation ID
     * @param replyTo The reply queue
     * @param request The request object
     * @throws IOException If sending fails
     */
    public void sendRequest(String exchange, String routingKey, String correlationId,
                            String replyTo, Object request) throws IOException {
        sendRequest(exchange, routingKey, correlationId, replyTo, request, null);
    }

    /**
     * Sends a request via RabbitMQ with an expiration.
     *
     * @param exchange The exchange to publish to
     * @param routingKey The routing key
     * @param correlationId The correlation ID
     * @param replyTo The reply queue
     * @param request The request object
     * @param expiration Message expiration in milliseconds as a string
     * @throws IOException If sending fails
     */
    public void sendRequest(String exchange, String routingKey, String correlationId,
                            String replyTo, Object request, String expiration) throws IOException {
        byte[] messageBody = objectMapper.writeValueAsBytes(request);

        AMQP.BasicProperties.Builder propsBuilder = new AMQP.BasicProperties.Builder()
                .correlationId(correlationId)
                .replyTo(replyTo);

        if (expiration != null) {
            propsBuilder.expiration(expiration);
        }

        channel.basicPublish(
                exchange,
                routingKey,
                propsBuilder.build(),
                messageBody);

        LOG.info("Sent request to {} with routing key {} and correlationId {}",
                exchange, routingKey, correlationId);
    }

    /**
     * Receives a response from a queue.
     *
     * @param queueName The queue to receive from
     * @param correlationId The expected correlation ID
     * @param timeoutSeconds Maximum time to wait in seconds
     * @return The response message body, or null if no response is received
     * @throws IOException If receiving fails
     * @throws InterruptedException If waiting is interrupted
     */
    public byte[] receiveResponse(String queueName, String correlationId,
                                  int timeoutSeconds) throws IOException, InterruptedException {
        long endTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(timeoutSeconds);

        while (System.currentTimeMillis() < endTime) {
            GetResponse response = channel.basicGet(queueName, true);

            if (response != null) {
                String msgCorrelationId = response.getProps().getCorrelationId();

                if (correlationId.equals(msgCorrelationId)) {
                    LOG.info("Received response with correlationId {}", correlationId);
                    return response.getBody();
                } else {
                    LOG.info("Received response with unexpected correlationId {}", msgCorrelationId);
                }
            }

            // Wait a bit before trying again
            Thread.sleep(100);
        }

        LOG.info("No response received with correlationId {} after {} seconds",
                correlationId, timeoutSeconds);
        return null;
    }

    /**
     * Deserializes a JSON message to a specified type.
     *
     * @param messageBody The message body bytes
     * @param type The target type
     * @return The deserialized object
     * @throws IOException If deserialization fails
     */
    public <T> T deserialize(byte[] messageBody, Class<T> type) throws IOException {
        if (messageBody == null) {
            return null;
        }
        return objectMapper.readValue(messageBody, type);
    }

    @Override
    public void close() throws IOException {
        try {
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
            if (connection != null && connection.isOpen()) {
                connection.close();
            }
        } catch (TimeoutException e) {
            throw new IOException("Timeout while closing RabbitMQ connection", e);
        }
    }
}