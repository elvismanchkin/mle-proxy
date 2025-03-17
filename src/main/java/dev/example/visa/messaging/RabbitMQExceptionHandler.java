package dev.example.visa.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;
import dev.example.visa.model.ErrorResponse;
import io.github.jopenlibs.vault.VaultException;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.rabbitmq.bind.RabbitConsumerState;
import io.micronaut.rabbitmq.exception.DefaultRabbitListenerExceptionHandler;
import io.micronaut.rabbitmq.exception.RabbitListenerException;
import io.micronaut.rabbitmq.exception.RabbitListenerExceptionHandler;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Custom exception handler for RabbitMQ message processing.
 * Handles exceptions thrown during message processing and sends appropriate error responses.
 */
@Slf4j
@Singleton
@Primary
@Replaces(DefaultRabbitListenerExceptionHandler.class)
public class RabbitMQExceptionHandler implements RabbitListenerExceptionHandler {

    private final ObjectMapper objectMapper;

    /**
     * Constructor with ObjectMapper for JSON serialization.
     */
    public RabbitMQExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(@NonNull RabbitListenerException exception) {
        log.error("Error processing RabbitMQ message", exception);

        exception.getMessageState().ifPresent(state -> {
            try {
                handleWithReply(exception, state);
            } catch (Exception e) {
                log.error("Failed to handle RabbitMQ exception", e);
            }
        });
    }

    /**
     * Handles the exception by sending an error response to the reply queue if available.
     */
    private void handleWithReply(RabbitListenerException exception, RabbitConsumerState state) throws Exception {
        Channel channel = state.getChannel();
        AMQP.BasicProperties properties = state.getProperties();
        Envelope envelope = state.getEnvelope();

        // Log the original message for debugging
        log.debug("Original message: Routing key={}, Exchange={}, DeliveryTag={}",
                envelope.getRoutingKey(), envelope.getExchange(), envelope.getDeliveryTag());

        // Skip reply if no replyTo is set
        if (properties.getReplyTo() == null) {
            log.debug("No replyTo address in properties, acknowledging message without reply");
            channel.basicAck(envelope.getDeliveryTag(), false);
            return;
        }

        Throwable cause = getRootCause(exception);
        int statusCode = determineStatusCode(cause);
        ErrorResponse errorResponse = createErrorResponse(cause);
        byte[] responseBody = objectMapper.writeValueAsBytes(errorResponse);

        // Create properties for the response message
        AMQP.BasicProperties replyProperties = new AMQP.BasicProperties.Builder()
                .correlationId(properties.getCorrelationId())
                .headers(createErrorHeaders(cause, statusCode))
                .build();

        // Send the error response to the reply queue
        channel.basicPublish("", properties.getReplyTo(), replyProperties, responseBody);

        // Acknowledge the original message after sending the error response
        channel.basicAck(envelope.getDeliveryTag(), false);

        log.debug("Sent error response to replyTo queue: {}", properties.getReplyTo());
    }

    /**
     * Gets the root cause of an exception.
     */
    private Throwable getRootCause(Throwable exception) {
        Throwable cause = exception;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        return cause;
    }

    /**
     * Determines the appropriate HTTP status code for an exception.
     */
    private int determineStatusCode(Throwable exception) {
        if (exception instanceof HttpClientResponseException httpException) {
            return httpException.getStatus().getCode();
        } else if (exception instanceof VaultException) {
            return 500; // Internal Server Error
        } else if (exception instanceof IllegalArgumentException) {
            return 400; // Bad Request
        } else {
            return 500; // Internal Server Error by default
        }
    }

    /**
     * Creates error headers to include in the response.
     */
    private Map<String, Object> createErrorHeaders(Throwable exception, int statusCode) {
        Map<String, Object> headers = new HashMap<>();
        headers.put("x-error-type", exception.getClass().getName());
        headers.put("x-error-status", statusCode);
        return headers;
    }

    /**
     * Creates an appropriate error response based on the exception.
     */
    private ErrorResponse createErrorResponse(Throwable exception) {
        if (exception instanceof HttpClientResponseException httpException) {
            Optional<ErrorResponse> visaErrorOpt = httpException.getResponse()
                    .getBody(ErrorResponse.class);

            if (visaErrorOpt.isPresent()) {
                return visaErrorOpt.get();
            }
        }

        return ErrorResponse.builder()
                .reason(exception.getClass().getSimpleName())
                .message(exception.getMessage())
                .details(Collections.emptyList())
                .build();
    }
}