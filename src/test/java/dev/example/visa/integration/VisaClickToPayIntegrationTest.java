package dev.example.visa.integration;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import dev.example.visa.client.VisaClickToPayClient;
import dev.example.visa.dto.AddressDto;
import dev.example.visa.dto.ConsumerDataResponseDto;
import dev.example.visa.dto.DeleteConsumerInformationRequestDto;
import dev.example.visa.dto.DeletePaymentInstrumentsRequestDto;
import dev.example.visa.dto.EnrollDataRequestDto;
import dev.example.visa.dto.EnrollPaymentInstrumentsRequestDto;
import dev.example.visa.dto.EnrollmentResponseDto;
import dev.example.visa.dto.GetDataRequestDto;
import dev.example.visa.dto.ManageConsumerInformationRequestDto;
import dev.example.visa.dto.ManagePaymentInstrumentsRequestDto;
import dev.example.visa.dto.RequestStatusResponseDto;
import dev.example.visa.model.DeleteConsumerInformationRequest;
import dev.example.visa.model.DeletePaymentInstrumentsRequest;
import dev.example.visa.model.EnrollDataRequest;
import dev.example.visa.model.EnrollPaymentInstrumentsRequest;
import dev.example.visa.model.GetDataRequest;
import dev.example.visa.model.GetDataResponse;
import dev.example.visa.model.ManageConsumerInformationRequest;
import dev.example.visa.model.ManagePaymentInstrumentsRequest;
import dev.example.visa.model.RequestIdResponse;
import dev.example.visa.model.RequestStatusResponse;
import io.micronaut.context.ApplicationContext;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.serde.ObjectMapper;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@MicronautTest
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class VisaClickToPayIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(VisaClickToPayIntegrationTest.class);

    @Container
    static RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:3.11-management")
            .withExposedPorts(5672, 15672);

    @Inject
    VisaClickToPayClient mockVisaClient;

    @Inject
    EmbeddedServer embeddedServer;

    @Inject
    ApplicationContext applicationContext;

    @Inject
    ObjectMapper objectMapper;

    private Connection rabbitConnection;
    private Channel rabbitChannel;
    private final String correlationId = UUID.randomUUID().toString();
    private final String consumerIdForTests = "test-consumer-" + UUID.randomUUID();

    @BeforeAll
    void setup() throws IOException, TimeoutException, URISyntaxException, NoSuchAlgorithmException, KeyManagementException {
        rabbitMQContainer.start();
        System.setProperty("rabbitmq.uri", rabbitMQContainer.getAmqpUrl());
        LOG.info("RabbitMQ container started at: {}", rabbitMQContainer.getAmqpUrl());

        // Setup RabbitMQ client
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUri(rabbitMQContainer.getAmqpUrl());
        rabbitConnection = factory.newConnection();
        rabbitChannel = rabbitConnection.createChannel();

        // Setup required exchanges and queues
        setupRabbitMQ();
    }

    private void setupRabbitMQ() throws IOException {
        // Define exchange
        String exchangeName = "visa-click-to-pay-exchange";
        rabbitChannel.exchangeDeclare(exchangeName, "direct", true);

        // Define queues and bindings
        String requestQueuePrefix = "visa-click-to-pay-requests";

        String[] operations = {
                "enrollData",
                "enrollPaymentInstruments",
                "requestStatus",
                "managePaymentInstruments",
                "manageConsumerInformation",
                "deleteConsumerInformation",
                "deletePaymentInstruments",
                "getData"
        };

        for (String operation : operations) {
            String queueName = requestQueuePrefix + "." + operation;
            rabbitChannel.queueDeclare(queueName, true, false, false, null);
            rabbitChannel.queueBind(queueName, exchangeName, operation);
            LOG.info("Declared and bound queue: {} with routing key: {}", queueName, operation);
        }
    }

    @BeforeEach
    void setupMocks() {
        Mockito.reset(mockVisaClient);

        // Mock the Visa Client responses
        when(mockVisaClient.enrollData(any(EnrollDataRequest.class), anyString()))
                .thenReturn(Mono.just(RequestIdResponse.builder().requestTraceId("trace-enroll-data").build()));

        when(mockVisaClient.enrollPaymentInstruments(any(EnrollPaymentInstrumentsRequest.class), anyString()))
                .thenReturn(Mono.just(RequestIdResponse.builder().requestTraceId("trace-enroll-payment").build()));

        when(mockVisaClient.getRequestStatus(anyString(), anyString()))
                .thenReturn(Mono.just(RequestStatusResponse.builder()
                        .status("COMPLETED")
                        .details(Collections.emptyList())
                        .build()));

        when(mockVisaClient.managePaymentInstruments(any(ManagePaymentInstrumentsRequest.class), anyString()))
                .thenReturn(Mono.just(RequestIdResponse.builder().requestTraceId("trace-manage-payment").build()));

        when(mockVisaClient.manageConsumerInformation(any(ManageConsumerInformationRequest.class), anyString()))
                .thenReturn(Mono.just(RequestIdResponse.builder().requestTraceId("trace-manage-consumer").build()));

        when(mockVisaClient.deleteConsumerInformation(any(DeleteConsumerInformationRequest.class), anyString()))
                .thenReturn(Mono.just(RequestIdResponse.builder().requestTraceId("trace-delete-consumer").build()));

        when(mockVisaClient.deletePaymentInstruments(any(DeletePaymentInstrumentsRequest.class), anyString()))
                .thenReturn(Mono.just(RequestIdResponse.builder().requestTraceId("trace-delete-payment").build()));

        when(mockVisaClient.getData(any(GetDataRequest.class), anyString()))
                .thenReturn(Mono.just(GetDataResponse.builder()
                        .data(Collections.emptyList())
                        .build()));
    }

    @AfterAll
    void tearDown() throws IOException, TimeoutException {
        if (rabbitChannel != null && rabbitChannel.isOpen()) {
            rabbitChannel.close();
        }
        if (rabbitConnection != null && rabbitConnection.isOpen()) {
            rabbitConnection.close();
        }
        if (rabbitMQContainer != null && rabbitMQContainer.isRunning()) {
            rabbitMQContainer.stop();
        }
    }

    @Test
    void testEnrollDataViaRPC() throws Exception {
        // Prepare request
        EnrollDataRequestDto request = buildEnrollDataRequest();

        // Send request via RPC
        CompletableFuture<EnrollmentResponseDto> responseFuture = new CompletableFuture<>();

        // Create temporary response queue
        String replyQueueName = rabbitChannel.queueDeclare().getQueue();

        // Set up consumer for the response
        rabbitChannel.basicConsume(replyQueueName, true, (consumerTag, message) -> {
            if (correlationId.equals(message.getProperties().getCorrelationId())) {
                String responseJson = new String(message.getBody(), StandardCharsets.UTF_8);
                EnrollmentResponseDto response = objectMapper.readValue(responseJson, EnrollmentResponseDto.class);
                responseFuture.complete(response);
            }
        }, consumerTag -> {});

        // Publish request
        rabbitChannel.basicPublish(
                "visa-click-to-pay-exchange",
                "enrollData",
                new com.rabbitmq.client.AMQP.BasicProperties.Builder()
                        .correlationId(correlationId)
                        .replyTo(replyQueueName)
                        .build(),
                objectMapper.writeValueAsBytes(request));

        // Wait for response
        EnrollmentResponseDto response = responseFuture.get(10, TimeUnit.SECONDS);

        // Verify
        assertNotNull(response);
        assertEquals("SUCCESS", response.status());
        assertEquals("trace-enroll-data", response.requestId());

        // Verify client was called with correct data
        ArgumentCaptor<EnrollDataRequest> requestCaptor = ArgumentCaptor.forClass(EnrollDataRequest.class);
        verify(mockVisaClient, times(1)).enrollData(requestCaptor.capture(), anyString());

        EnrollDataRequest capturedRequest = requestCaptor.getValue();
        assertEquals("PRODUCT_CODE", capturedRequest.intent().type());
        assertEquals("CLICK_TO_PAY", capturedRequest.intent().value());
        assertEquals(consumerIdForTests, capturedRequest.consumerInformation().externalConsumerID());
        assertEquals("John", capturedRequest.consumerInformation().firstName());
        assertEquals("Doe", capturedRequest.consumerInformation().lastName());
    }

    @Test
    void testEnrollPaymentInstrumentsViaRPC() throws Exception {
        // Prepare request
        EnrollPaymentInstrumentsRequestDto request = buildEnrollPaymentInstrumentsRequest();

        // Send request via RPC
        CompletableFuture<EnrollmentResponseDto> responseFuture = new CompletableFuture<>();

        // Create temporary response queue
        String replyQueueName = rabbitChannel.queueDeclare().getQueue();

        // Set up consumer for the response
        rabbitChannel.basicConsume(replyQueueName, true, (consumerTag, message) -> {
            if (correlationId.equals(message.getProperties().getCorrelationId())) {
                String responseJson = new String(message.getBody(), StandardCharsets.UTF_8);
                EnrollmentResponseDto response = objectMapper.readValue(responseJson, EnrollmentResponseDto.class);
                responseFuture.complete(response);
            }
        }, consumerTag -> {});

        // Publish request
        rabbitChannel.basicPublish(
                "visa-click-to-pay-exchange",
                "enrollPaymentInstruments",
                new com.rabbitmq.client.AMQP.BasicProperties.Builder()
                        .correlationId(correlationId)
                        .replyTo(replyQueueName)
                        .build(),
                objectMapper.writeValueAsBytes(request));

        // Wait for response
        EnrollmentResponseDto response = responseFuture.get(10, TimeUnit.SECONDS);

        // Verify
        assertNotNull(response);
        assertEquals("SUCCESS", response.status());
        assertEquals("trace-enroll-payment", response.requestId());

        // Verify client was called with correct data
        ArgumentCaptor<EnrollPaymentInstrumentsRequest> requestCaptor =
                ArgumentCaptor.forClass(EnrollPaymentInstrumentsRequest.class);
        verify(mockVisaClient, times(1)).enrollPaymentInstruments(requestCaptor.capture(), anyString());

        EnrollPaymentInstrumentsRequest capturedRequest = requestCaptor.getValue();
        assertEquals("PRODUCT_CODE", capturedRequest.intent().type());
        assertEquals("CLICK_TO_PAY", capturedRequest.intent().value());
        assertEquals(consumerIdForTests, capturedRequest.consumerInformation().externalConsumerID());
    }

    @Test
    void testGetRequestStatusViaRPC() throws Exception {
        // Request trace ID to check
        String requestTraceId = "test-trace-" + UUID.randomUUID();

        // Send request via RPC
        CompletableFuture<RequestStatusResponseDto> responseFuture = new CompletableFuture<>();

        // Create temporary response queue
        String replyQueueName = rabbitChannel.queueDeclare().getQueue();

        // Set up consumer for the response
        rabbitChannel.basicConsume(replyQueueName, true, (consumerTag, message) -> {
            if (correlationId.equals(message.getProperties().getCorrelationId())) {
                String responseJson = new String(message.getBody(), StandardCharsets.UTF_8);
                RequestStatusResponseDto response = objectMapper.readValue(
                        responseJson, RequestStatusResponseDto.class);
                responseFuture.complete(response);
            }
        }, consumerTag -> {});

        // Publish request
        rabbitChannel.basicPublish(
                "visa-click-to-pay-exchange",
                "requestStatus",
                new com.rabbitmq.client.AMQP.BasicProperties.Builder()
                        .correlationId(correlationId)
                        .replyTo(replyQueueName)
                        .build(),
                requestTraceId.getBytes(StandardCharsets.UTF_8));

        // Wait for response
        RequestStatusResponseDto response = responseFuture.get(10, TimeUnit.SECONDS);

        // Verify
        assertNotNull(response);
        assertEquals("COMPLETED", response.status());

        // Verify client was called with correct data
        verify(mockVisaClient, times(1)).getRequestStatus(requestTraceId, anyString());
    }

    @Test
    void testManagePaymentInstrumentsViaRPC() throws Exception {
        // Prepare request
        ManagePaymentInstrumentsRequestDto request = buildManagePaymentInstrumentsRequest();

        // Send request via RPC
        CompletableFuture<EnrollmentResponseDto> responseFuture = new CompletableFuture<>();

        // Create temporary response queue
        String replyQueueName = rabbitChannel.queueDeclare().getQueue();

        // Set up consumer for the response
        rabbitChannel.basicConsume(replyQueueName, true, (consumerTag, message) -> {
            if (correlationId.equals(message.getProperties().getCorrelationId())) {
                String responseJson = new String(message.getBody(), StandardCharsets.UTF_8);
                EnrollmentResponseDto response = objectMapper.readValue(responseJson, EnrollmentResponseDto.class);
                responseFuture.complete(response);
            }
        }, consumerTag -> {});

        // Publish request
        rabbitChannel.basicPublish(
                "visa-click-to-pay-exchange",
                "managePaymentInstruments",
                new com.rabbitmq.client.AMQP.BasicProperties.Builder()
                        .correlationId(correlationId)
                        .replyTo(replyQueueName)
                        .build(),
                objectMapper.writeValueAsBytes(request));

        // Wait for response
        EnrollmentResponseDto response = responseFuture.get(10, TimeUnit.SECONDS);

        // Verify
        assertNotNull(response);
        assertEquals("SUCCESS", response.status());
        assertEquals("trace-manage-payment", response.requestId());

        // Verify client was called with correct data
        ArgumentCaptor<ManagePaymentInstrumentsRequest> requestCaptor =
                ArgumentCaptor.forClass(ManagePaymentInstrumentsRequest.class);
        verify(mockVisaClient, times(1)).managePaymentInstruments(requestCaptor.capture(), anyString());

        ManagePaymentInstrumentsRequest capturedRequest = requestCaptor.getValue();
        assertEquals("PRODUCT_CODE", capturedRequest.intent().type());
        assertEquals("CLICK_TO_PAY", capturedRequest.intent().value());
        assertEquals(consumerIdForTests, capturedRequest.consumerInformation().externalConsumerID());
    }

    @Test
    void testManageConsumerInformationViaRPC() throws Exception {
        // Prepare request
        ManageConsumerInformationRequestDto request = buildManageConsumerInformationRequest();

        // Send request via RPC
        CompletableFuture<EnrollmentResponseDto> responseFuture = new CompletableFuture<>();

        // Create temporary response queue
        String replyQueueName = rabbitChannel.queueDeclare().getQueue();

        // Set up consumer for the response
        rabbitChannel.basicConsume(replyQueueName, true, (consumerTag, message) -> {
            if (correlationId.equals(message.getProperties().getCorrelationId())) {
                String responseJson = new String(message.getBody(), StandardCharsets.UTF_8);
                EnrollmentResponseDto response = objectMapper.readValue(responseJson, EnrollmentResponseDto.class);
                responseFuture.complete(response);
            }
        }, consumerTag -> {});

        // Publish request
        rabbitChannel.basicPublish(
                "visa-click-to-pay-exchange",
                "manageConsumerInformation",
                new com.rabbitmq.client.AMQP.BasicProperties.Builder()
                        .correlationId(correlationId)
                        .replyTo(replyQueueName)
                        .build(),
                objectMapper.writeValueAsBytes(request));

        // Wait for response
        EnrollmentResponseDto response = responseFuture.get(10, TimeUnit.SECONDS);

        // Verify
        assertNotNull(response);
        assertEquals("SUCCESS", response.status());
        assertEquals("trace-manage-consumer", response.requestId());

        // Verify client was called with correct data
        ArgumentCaptor<ManageConsumerInformationRequest> requestCaptor =
                ArgumentCaptor.forClass(ManageConsumerInformationRequest.class);
        verify(mockVisaClient, times(1)).manageConsumerInformation(requestCaptor.capture(), anyString());

        ManageConsumerInformationRequest capturedRequest = requestCaptor.getValue();
        assertEquals("PRODUCT_CODE", capturedRequest.intent().type());
        assertEquals("CLICK_TO_PAY", capturedRequest.intent().value());
        assertEquals(consumerIdForTests, capturedRequest.consumerInformation().externalConsumerID());
        assertEquals("John", capturedRequest.consumerInformation().firstName());
        assertEquals("Doe", capturedRequest.consumerInformation().lastName());
    }

    @Test
    void testDeleteConsumerInformationViaRPC() throws Exception {
        // Prepare request
        DeleteConsumerInformationRequestDto request = buildDeleteConsumerInformationRequest();

        // Send request via RPC
        CompletableFuture<EnrollmentResponseDto> responseFuture = new CompletableFuture<>();

        // Create temporary response queue
        String replyQueueName = rabbitChannel.queueDeclare().getQueue();

        // Set up consumer for the response
        rabbitChannel.basicConsume(replyQueueName, true, (consumerTag, message) -> {
            if (correlationId.equals(message.getProperties().getCorrelationId())) {
                String responseJson = new String(message.getBody(), StandardCharsets.UTF_8);
                EnrollmentResponseDto response = objectMapper.readValue(responseJson, EnrollmentResponseDto.class);
                responseFuture.complete(response);
            }
        }, consumerTag -> {});

        // Publish request
        rabbitChannel.basicPublish(
                "visa-click-to-pay-exchange",
                "deleteConsumerInformation",
                new com.rabbitmq.client.AMQP.BasicProperties.Builder()
                        .correlationId(correlationId)
                        .replyTo(replyQueueName)
                        .build(),
                objectMapper.writeValueAsBytes(request));

        // Wait for response
        EnrollmentResponseDto response = responseFuture.get(10, TimeUnit.SECONDS);

        // Verify
        assertNotNull(response);
        assertEquals("SUCCESS", response.status());
        assertEquals("trace-delete-consumer", response.requestId());

        // Verify client was called with correct data
        ArgumentCaptor<DeleteConsumerInformationRequest> requestCaptor =
                ArgumentCaptor.forClass(DeleteConsumerInformationRequest.class);
        verify(mockVisaClient, times(1)).deleteConsumerInformation(requestCaptor.capture(), anyString());

        DeleteConsumerInformationRequest capturedRequest = requestCaptor.getValue();
        assertEquals("PRODUCT_CODE", capturedRequest.intent().type());
        assertEquals("CLICK_TO_PAY", capturedRequest.intent().value());
        assertEquals(consumerIdForTests, capturedRequest.consumerInformation().externalConsumerID());
    }

    @Test
    void testDeletePaymentInstrumentsViaRPC() throws Exception {
        // Prepare request
        DeletePaymentInstrumentsRequestDto request = buildDeletePaymentInstrumentsRequest();

        // Send request via RPC
        CompletableFuture<EnrollmentResponseDto> responseFuture = new CompletableFuture<>();

        // Create temporary response queue
        String replyQueueName = rabbitChannel.queueDeclare().getQueue();

        // Set up consumer for the response
        rabbitChannel.basicConsume(replyQueueName, true, (consumerTag, message) -> {
            if (correlationId.equals(message.getProperties().getCorrelationId())) {
                String responseJson = new String(message.getBody(), StandardCharsets.UTF_8);
                EnrollmentResponseDto response = objectMapper.readValue(responseJson, EnrollmentResponseDto.class);
                responseFuture.complete(response);
            }
        }, consumerTag -> {});

        // Publish request
        rabbitChannel.basicPublish(
                "visa-click-to-pay-exchange",
                "deletePaymentInstruments",
                new com.rabbitmq.client.AMQP.BasicProperties.Builder()
                        .correlationId(correlationId)
                        .replyTo(replyQueueName)
                        .build(),
                objectMapper.writeValueAsBytes(request));

        // Wait for response
        EnrollmentResponseDto response = responseFuture.get(10, TimeUnit.SECONDS);

        // Verify
        assertNotNull(response);
        assertEquals("SUCCESS", response.status());
        assertEquals("trace-delete-payment", response.requestId());

        // Verify client was called with correct data
        ArgumentCaptor<DeletePaymentInstrumentsRequest> requestCaptor =
                ArgumentCaptor.forClass(DeletePaymentInstrumentsRequest.class);
        verify(mockVisaClient, times(1)).deletePaymentInstruments(requestCaptor.capture(), anyString());

        DeletePaymentInstrumentsRequest capturedRequest = requestCaptor.getValue();
        assertEquals("PRODUCT_CODE", capturedRequest.intent().type());
        assertEquals("CLICK_TO_PAY", capturedRequest.intent().value());
        assertEquals(consumerIdForTests, capturedRequest.consumerInformation().externalConsumerID());
    }

    @Test
    void testGetDataViaRPC() throws Exception {
        // Prepare request
        GetDataRequestDto request = buildGetDataRequest();

        // Send request via RPC
        CompletableFuture<ConsumerDataResponseDto> responseFuture = new CompletableFuture<>();

        // Create temporary response queue
        String replyQueueName = rabbitChannel.queueDeclare().getQueue();

        // Set up consumer for the response
        rabbitChannel.basicConsume(replyQueueName, true, (consumerTag, message) -> {
            if (correlationId.equals(message.getProperties().getCorrelationId())) {
                String responseJson = new String(message.getBody(), StandardCharsets.UTF_8);
                ConsumerDataResponseDto response = objectMapper.readValue(responseJson, ConsumerDataResponseDto.class);
                responseFuture.complete(response);
            }
        }, consumerTag -> {});

        // Publish request
        rabbitChannel.basicPublish(
                "visa-click-to-pay-exchange",
                "getData",
                new com.rabbitmq.client.AMQP.BasicProperties.Builder()
                        .correlationId(correlationId)
                        .replyTo(replyQueueName)
                        .build(),
                objectMapper.writeValueAsBytes(request));

        // Wait for response
        ConsumerDataResponseDto response = responseFuture.get(10, TimeUnit.SECONDS);

        // Verify
        assertNotNull(response);
        assertEquals("SUCCESS", response.status());

        // Verify client was called with correct data
        ArgumentCaptor<GetDataRequest> requestCaptor = ArgumentCaptor.forClass(GetDataRequest.class);
        verify(mockVisaClient, times(1)).getData(requestCaptor.capture(), anyString());

        GetDataRequest capturedRequest = requestCaptor.getValue();
        assertEquals("PRODUCT_CODE", capturedRequest.intent().type());
        assertEquals("CLICK_TO_PAY", capturedRequest.intent().value());
        assertEquals(consumerIdForTests, capturedRequest.consumerInformation().externalConsumerID());
    }

    @Test
    void testErrorHandlingViaRPC() throws Exception {
        // Prepare a request
        EnrollDataRequestDto request = buildEnrollDataRequest();

        // Mock an error response from the Visa client
        HttpClientResponseException mockException = Mockito.mock(HttpClientResponseException.class);
        HttpResponse mockResponse = Mockito.mock(HttpResponse.class);
        when(mockException.getResponse()).thenReturn(mockResponse);
        when(mockResponse.getBody(any(Argument.class))).thenReturn(Optional.empty());
        when(mockVisaClient.enrollData(any(), anyString())).thenReturn(Mono.error(mockException));

        // Send request via RPC
        CompletableFuture<EnrollmentResponseDto> responseFuture = new CompletableFuture<>();

        // Create temporary response queue
        String replyQueueName = rabbitChannel.queueDeclare().getQueue();

        // Set up consumer for the response
        rabbitChannel.basicConsume(replyQueueName, true, (consumerTag, message) -> {
            if (correlationId.equals(message.getProperties().getCorrelationId())) {
                String responseJson = new String(message.getBody(), StandardCharsets.UTF_8);
                EnrollmentResponseDto response = objectMapper.readValue(responseJson, EnrollmentResponseDto.class);
                responseFuture.complete(response);
            }
        }, consumerTag -> {});

        // Publish request
        rabbitChannel.basicPublish(
                "visa-click-to-pay-exchange",
                "enrollData",
                new com.rabbitmq.client.AMQP.BasicProperties.Builder()
                        .correlationId(correlationId)
                        .replyTo(replyQueueName)
                        .build(),
                objectMapper.writeValueAsBytes(request));

        // Wait for response
        EnrollmentResponseDto response = responseFuture.get(10, TimeUnit.SECONDS);

        // Verify
        assertNotNull(response);
        assertEquals("ERROR", response.status());
        assertNotNull(response.error());
    }

    @Test
    void testConcurrentRequestsViaRPC() throws Exception {
        // Number of concurrent requests
        final int REQUEST_COUNT = 5;

        // Prepare for multiple concurrent requests
        CountDownLatch latch = new CountDownLatch(REQUEST_COUNT);

        // Create temporary response queue
        String replyQueueName = rabbitChannel.queueDeclare().getQueue();

        // Set up consumer for the responses
        rabbitChannel.basicConsume(replyQueueName, true, (consumerTag, message) -> {
            try {
                // Check if this is one of our test messages
                if (message.getProperties().getCorrelationId().startsWith("test-concurrent-")) {
                    String responseJson = new String(message.getBody(), StandardCharsets.UTF_8);
                    EnrollmentResponseDto response = objectMapper.readValue(
                            responseJson, EnrollmentResponseDto.class);

                    // Basic validation
                    assertEquals("SUCCESS", response.status());
                    assertTrue(response.requestId().startsWith("trace-"));

                    // Count down the latch
                    latch.countDown();
                }
            } catch (Exception e) {
                LOG.error("Error processing response", e);
            }
        }, consumerTag -> {});

        // Send multiple requests
        for (int i = 0; i < REQUEST_COUNT; i++) {
            EnrollDataRequestDto request = buildEnrollDataRequest();

            // Use different correlation IDs for each request
            String requestCorrelationId = "test-concurrent-" + i;

            // Publish request
            rabbitChannel.basicPublish(
                    "visa-click-to-pay-exchange",
                    "enrollData",
                    new com.rabbitmq.client.AMQP.BasicProperties.Builder()
                            .correlationId(requestCorrelationId)
                            .replyTo(replyQueueName)
                            .build(),
                    objectMapper.writeValueAsBytes(request));
        }

        // Wait for all responses
        boolean allResponses = latch.await(20, TimeUnit.SECONDS);

        // Verify
        assertTrue(allResponses, "Did not receive all concurrent responses in time");
        verify(mockVisaClient, times(REQUEST_COUNT)).enrollData(any(EnrollDataRequest.class), anyString());
    }

    @Test
    void testErrorPropagationForHttpClientErrors() throws Exception {
        // Prepare request
        GetDataRequestDto request = buildGetDataRequest();

        // Mock a client error (4xx)
        HttpClientResponseException clientException = Mockito.mock(HttpClientResponseException.class);
        HttpResponse errorResponse = Mockito.mock(HttpResponse.class);

        when(clientException.getResponse()).thenReturn(errorResponse);
        when(errorResponse.getBody(any(Argument.class))).thenReturn(Optional.of(
                dev.example.visa.model.ErrorResponse.builder()
                        .reason("InvalidParameter")
                        .message("Invalid consumer ID")
                        .build()));
        when(mockVisaClient.getData(any(), anyString())).thenReturn(Mono.error(clientException));

        // Send request via RPC
        CompletableFuture<ConsumerDataResponseDto> responseFuture = new CompletableFuture<>();

        // Create temporary response queue
        String replyQueueName = rabbitChannel.queueDeclare().getQueue();

        // Set up consumer for the response
        rabbitChannel.basicConsume(replyQueueName, true, (consumerTag, message) -> {
            if (correlationId.equals(message.getProperties().getCorrelationId())) {
                String responseJson = new String(message.getBody(), StandardCharsets.UTF_8);
                ConsumerDataResponseDto response = objectMapper.readValue(responseJson, ConsumerDataResponseDto.class);
                responseFuture.complete(response);
            }
        }, consumerTag -> {});

        // Publish request
        rabbitChannel.basicPublish(
                "visa-click-to-pay-exchange",
                "getData",
                new com.rabbitmq.client.AMQP.BasicProperties.Builder()
                        .correlationId(correlationId)
                        .replyTo(replyQueueName)
                        .build(),
                objectMapper.writeValueAsBytes(request));

        // Wait for response
        ConsumerDataResponseDto response = responseFuture.get(10, TimeUnit.SECONDS);

        // Verify
        assertNotNull(response);
        assertEquals("ERROR", response.status());
        assertNotNull(response.error());
        assertEquals("InvalidParameter", response.error().reason());
        assertEquals("Invalid consumer ID", response.error().message());
    }

    @Test
    void testTimeoutHandling() throws Exception {
        // Prepare request
        GetDataRequestDto request = buildGetDataRequest();

        // Mock a delay longer than RPC timeout
        when(mockVisaClient.getData(any(), anyString()))
                .thenReturn(Mono.delay(Duration.ofSeconds(10))
                        .then(Mono.just(GetDataResponse.builder().data(Collections.emptyList()).build())));

        // Send request via RPC
        CompletableFuture<Boolean> timeoutDetected = new CompletableFuture<>();

        // Create temporary response queue with short TTL for messages
        Map<String, Object> queueArgs = Map.of("x-message-ttl", 3000); // 3 seconds TTL
        String replyQueueName = rabbitChannel.queueDeclare("", false, true, true, queueArgs).getQueue();

        // Set up consumer for the response with timeout detection
        rabbitChannel.basicConsume(replyQueueName, true, (consumerTag, message) -> {
            // We should not receive a normal response
            if (correlationId.equals(message.getProperties().getCorrelationId())) {
                timeoutDetected.complete(false);
            }
        }, consumerTag -> {
            // Consumer cancelled (e.g., queue deleted due to timeout)
            timeoutDetected.complete(true);
        });

        // Publish request
        rabbitChannel.basicPublish(
                "visa-click-to-pay-exchange",
                "getData",
                new com.rabbitmq.client.AMQP.BasicProperties.Builder()
                        .correlationId(correlationId)
                        .replyTo(replyQueueName)
                        .expiration("3000") // 3 seconds until message expires
                        .build(),
                objectMapper.writeValueAsBytes(request));

        // We expect the operation to time out
        try {
            Boolean result = timeoutDetected.get(5, TimeUnit.SECONDS);
            // If we have a concrete result (true for timeout detected, false for unexpected response)
            if (result != null && !result) {
                // We got a response when we expected a timeout
                assertTrue(false, "Received response when timeout was expected");
            }
        } catch (TimeoutException e) {
            // This is also acceptable - the CompletableFuture itself timed out
            // which likely means our RPC timeout mechanism worked
        }

        // The operation should have been called, but we don't get a response in time
        verify(mockVisaClient, times(1)).getData(any(GetDataRequest.class), anyString());
    }

    // Edge case: Bank account enrollment
    @Test
    void testEnrollBankAccountPaymentInstrument() throws Exception {
        // Prepare request - note we're using bank account this time, not card
        EnrollPaymentInstrumentsRequestDto request = EnrollPaymentInstrumentsRequestDto.builder()
                .intentType("PRODUCT_CODE")
                .intentValue("CLICK_TO_PAY")
                .consumerId(consumerIdForTests)
                .paymentType("BANK_ACCOUNT") // Bank Account instead of Card
                .accountNumber("12345678901234")
                .accountName("John Doe")
                .accountType("CHECKING")
                .bankName("Test Bank")
                .bankCode("123456789")
                .branchCode("001")
                .bankCodeType("DEFAULT")
                .currencyCode("USD")
                .accountNumberType("DEFAULT")
                .build();

        // Send request via RPC
        CompletableFuture<EnrollmentResponseDto> responseFuture = new CompletableFuture<>();

        // Create temporary response queue
        String replyQueueName = rabbitChannel.queueDeclare().getQueue();

        // Set up consumer for the response
        rabbitChannel.basicConsume(replyQueueName, true, (consumerTag, message) -> {
            if (correlationId.equals(message.getProperties().getCorrelationId())) {
                String responseJson = new String(message.getBody(), StandardCharsets.UTF_8);
                EnrollmentResponseDto response = objectMapper.readValue(responseJson, EnrollmentResponseDto.class);
                responseFuture.complete(response);
            }
        }, consumerTag -> {});

        // Publish request
        rabbitChannel.basicPublish(
                "visa-click-to-pay-exchange",
                "enrollPaymentInstruments",
                new com.rabbitmq.client.AMQP.BasicProperties.Builder()
                        .correlationId(correlationId)
                        .replyTo(replyQueueName)
                        .build(),
                objectMapper.writeValueAsBytes(request));

        // Wait for response
        EnrollmentResponseDto response = responseFuture.get(10, TimeUnit.SECONDS);

        // Verify
        assertNotNull(response);
        assertEquals("SUCCESS", response.status());

        // Verify client was called with correct data
        ArgumentCaptor<EnrollPaymentInstrumentsRequest> requestCaptor =
                ArgumentCaptor.forClass(EnrollPaymentInstrumentsRequest.class);
        verify(mockVisaClient, times(1)).enrollPaymentInstruments(requestCaptor.capture(), anyString());

        EnrollPaymentInstrumentsRequest capturedRequest = requestCaptor.getValue();
        assertEquals("PRODUCT_CODE", capturedRequest.intent().type());
        assertEquals("CLICK_TO_PAY", capturedRequest.intent().value());
        assertEquals(consumerIdForTests, capturedRequest.consumerInformation().externalConsumerID());

        // Verify payment instrument is of bank account type
        Object paymentInstrument = capturedRequest.paymentInstruments().get(0);
        // Since we can't directly cast to BankAccountPaymentInstrument here,
        // we verify by checking if it contains bank-specific properties
        String paymentJson = objectMapper.writeValueAsString(paymentInstrument);
        assertTrue(paymentJson.contains("\"type\":\"BANK_ACCOUNT\""));
        assertTrue(paymentJson.contains("\"accountNumber\":\"12345678901234\""));
        assertTrue(paymentJson.contains("\"accountName\":\"John Doe\""));
        assertTrue(paymentJson.contains("\"accountType\":\"CHECKING\""));
    }

    // Helper methods to build test requests

    private EnrollDataRequestDto buildEnrollDataRequest() {
        return EnrollDataRequestDto.builder()
                .intentType("PRODUCT_CODE")
                .intentValue("CLICK_TO_PAY")
                .consumerId(consumerIdForTests)
                .firstName("John")
                .lastName("Doe")
                .middleName("A")
                .countryCode("USA")
                .emails(List.of("john.doe@example.com"))
                .phones(List.of("16505551234"))
                .locale("en_US")
                .consentVersion("1.0")
                .consentPresenter("Test Bank")
                .consentTimestamp("2023-01-01T12:00:00.000Z")
                .paymentType("CARD")
                .cardNumber("4111111111111111")
                .cardType("Visa")
                .nameOnCard("John A Doe")
                .expirationDate("2025-12")
                .issuerName("Test Bank")
                .billingAddress(AddressDto.builder()
                        .addressLine1("123 Main St")
                        .addressLine2("Apt 4B")
                        .city("San Francisco")
                        .state("CA")
                        .country("USA")
                        .postalCode("94105")
                        .build())
                .build();
    }

    private EnrollPaymentInstrumentsRequestDto buildEnrollPaymentInstrumentsRequest() {
        return EnrollPaymentInstrumentsRequestDto.builder()
                .intentType("PRODUCT_CODE")
                .intentValue("CLICK_TO_PAY")
                .consumerId(consumerIdForTests)
                .paymentType("CARD")
                .cardNumber("4111111111111111")
                .cardType("Visa")
                .nameOnCard("John A Doe")
                .expirationDate("2025-12")
                .issuerName("Test Bank")
                .billingAddress(AddressDto.builder()
                        .addressLine1("123 Main St")
                        .city("San Francisco")
                        .state("CA")
                        .country("USA")
                        .postalCode("94105")
                        .build())
                .build();
    }

    private ManagePaymentInstrumentsRequestDto buildManagePaymentInstrumentsRequest() {
        return ManagePaymentInstrumentsRequestDto.builder()
                .intentType("PRODUCT_CODE")
                .intentValue("CLICK_TO_PAY")
                .consumerId(consumerIdForTests)
                .paymentType("CARD")
                .cardNumber("4111111111111111")
                .cardType("Visa")
                .nameOnCard("John A Doe")
                .expirationDate("2026-12") // Updated expiration date
                .issuerName("Test Bank")
                .billingAddress(AddressDto.builder()
                        .addressLine1("456 New St") // Updated address
                        .city("San Francisco")
                        .state("CA")
                        .country("USA")
                        .postalCode("94105")
                        .build())
                .build();
    }

    private ManageConsumerInformationRequestDto buildManageConsumerInformationRequest() {
        return ManageConsumerInformationRequestDto.builder()
                .intentType("PRODUCT_CODE")
                .intentValue("CLICK_TO_PAY")
                .consumerId(consumerIdForTests)
                .firstName("John")
                .lastName("Doe")
                .middleName("A")
                .countryCode("USA")
                .emails(List.of("new.email@example.com")) // Updated email
                .phones(List.of("16505551234"))
                .locale("en_US")
                .status("ACTIVE")
                .build();
    }

    private DeleteConsumerInformationRequestDto buildDeleteConsumerInformationRequest() {
        return DeleteConsumerInformationRequestDto.builder()
                .intentType("PRODUCT_CODE")
                .intentValue("CLICK_TO_PAY")
                .consumerId(consumerIdForTests)
                .build();
    }

    private DeletePaymentInstrumentsRequestDto buildDeletePaymentInstrumentsRequest() {
        return DeletePaymentInstrumentsRequestDto.builder()
                .intentType("PRODUCT_CODE")
                .intentValue("CLICK_TO_PAY")
                .consumerId(consumerIdForTests)
                .paymentType("CARD")
                .cardNumber("4111111111111111")
                .build();
    }

    private GetDataRequestDto buildGetDataRequest() {
        return GetDataRequestDto.builder()
                .intentType("PRODUCT_CODE")
                .intentValue("CLICK_TO_PAY")
                .consumerId(consumerIdForTests)
                .build();
    }
}