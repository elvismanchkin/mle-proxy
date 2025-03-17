package dev.example.visa.messaging;

import dev.example.visa.client.VisaClickToPayClient;
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
import io.micronaut.context.annotation.Value;
import io.micronaut.rabbitmq.annotation.Queue;
import io.micronaut.rabbitmq.annotation.RabbitListener;
import io.micronaut.tracing.annotation.ContinueSpan;
import io.micronaut.tracing.annotation.SpanTag;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * RabbitMQ message handler for Visa Click to Pay API operations.
 * Implements the RPC pattern for asynchronous communication.
 */
@Slf4j
@Singleton
@RabbitListener
public class VisaClickToPayMessageHandler {

    private final VisaClickToPayClient visaClient;

    @Value("${rabbitmq.rpc.request-queue:visa-click-to-pay-requests}")
    private String requestQueuePrefix;

    public VisaClickToPayMessageHandler(VisaClickToPayClient visaClient) {
        this.visaClient = visaClient;
    }

    @Queue("${rabbitmq.rpc.request-queue:visa-click-to-pay-requests}.enrollData")
    @ContinueSpan
    public Mono<RequestIdResponse> enrollData(
            @SpanTag("enrollData.request") EnrollDataRequest request,
            @SpanTag("correlationId") String correlationId) {

        String traceId = getTraceId(correlationId);
        try (MDC.MDCCloseable ignored = MDC.putCloseable("traceId", traceId)) {
            log.info("Processing enrollData request with correlationId: {}", correlationId);
            return visaClient.enrollData(request, traceId)
                    .doOnSuccess(response -> log.info("Successfully processed enrollData request"))
                    .doOnError(e -> log.error("Error processing enrollData request", e));
        }
    }

    @Queue("${rabbitmq.rpc.request-queue:visa-click-to-pay-requests}.enrollPaymentInstruments")
    @ContinueSpan
    public Mono<RequestIdResponse> enrollPaymentInstruments(
            @SpanTag("enrollPaymentInstruments.request") EnrollPaymentInstrumentsRequest request,
            @SpanTag("correlationId") String correlationId) {

        String traceId = getTraceId(correlationId);
        try (MDC.MDCCloseable ignored = MDC.putCloseable("traceId", traceId)) {
            log.info("Processing enrollPaymentInstruments request with correlationId: {}", correlationId);
            return visaClient.enrollPaymentInstruments(request, traceId)
                    .doOnSuccess(response -> log.info("Successfully processed enrollPaymentInstruments request"))
                    .doOnError(e -> log.error("Error processing enrollPaymentInstruments request", e));
        }
    }

    @Queue("${rabbitmq.rpc.request-queue:visa-click-to-pay-requests}.requestStatus")
    @ContinueSpan
    public Mono<RequestStatusResponse> requestStatus(
            @SpanTag("requestStatus.requestTraceId") String requestTraceId,
            @SpanTag("correlationId") String correlationId) {

        String traceId = getTraceId(correlationId);
        try (MDC.MDCCloseable ignored = MDC.putCloseable("traceId", traceId)) {
            log.info("Processing requestStatus for requestTraceId: {} with correlationId: {}",
                    requestTraceId, correlationId);
            return visaClient.getRequestStatus(requestTraceId, traceId)
                    .doOnSuccess(response -> log.info("Successfully retrieved request status"))
                    .doOnError(e -> log.error("Error retrieving request status", e));
        }
    }

    @Queue("${rabbitmq.rpc.request-queue:visa-click-to-pay-requests}.managePaymentInstruments")
    @ContinueSpan
    public Mono<RequestIdResponse> managePaymentInstruments(
            @SpanTag("managePaymentInstruments.request") ManagePaymentInstrumentsRequest request,
            @SpanTag("correlationId") String correlationId) {

        String traceId = getTraceId(correlationId);
        try (MDC.MDCCloseable ignored = MDC.putCloseable("traceId", traceId)) {
            log.info("Processing managePaymentInstruments request with correlationId: {}", correlationId);
            return visaClient.managePaymentInstruments(request, traceId)
                    .doOnSuccess(response -> log.info("Successfully processed managePaymentInstruments request"))
                    .doOnError(e -> log.error("Error processing managePaymentInstruments request", e));
        }
    }

    @Queue("${rabbitmq.rpc.request-queue:visa-click-to-pay-requests}.manageConsumerInformation")
    @ContinueSpan
    public Mono<RequestIdResponse> manageConsumerInformation(
            @SpanTag("manageConsumerInformation.request") ManageConsumerInformationRequest request,
            @SpanTag("correlationId") String correlationId) {

        String traceId = getTraceId(correlationId);
        try (MDC.MDCCloseable ignored = MDC.putCloseable("traceId", traceId)) {
            log.info("Processing manageConsumerInformation request with correlationId: {}", correlationId);
            return visaClient.manageConsumerInformation(request, traceId)
                    .doOnSuccess(response -> log.info("Successfully processed manageConsumerInformation request"))
                    .doOnError(e -> log.error("Error processing manageConsumerInformation request", e));
        }
    }

    @Queue("${rabbitmq.rpc.request-queue:visa-click-to-pay-requests}.deleteConsumerInformation")
    @ContinueSpan
    public Mono<RequestIdResponse> deleteConsumerInformation(
            @SpanTag("deleteConsumerInformation.request") DeleteConsumerInformationRequest request,
            @SpanTag("correlationId") String correlationId) {

        String traceId = getTraceId(correlationId);
        try (MDC.MDCCloseable ignored = MDC.putCloseable("traceId", traceId)) {
            log.info("Processing deleteConsumerInformation request with correlationId: {}", correlationId);
            return visaClient.deleteConsumerInformation(request, traceId)
                    .doOnSuccess(response -> log.info("Successfully processed deleteConsumerInformation request"))
                    .doOnError(e -> log.error("Error processing deleteConsumerInformation request", e));
        }
    }

    @Queue("${rabbitmq.rpc.request-queue:visa-click-to-pay-requests}.deletePaymentInstruments")
    @ContinueSpan
    public Mono<RequestIdResponse> deletePaymentInstruments(
            @SpanTag("deletePaymentInstruments.request") DeletePaymentInstrumentsRequest request,
            @SpanTag("correlationId") String correlationId) {

        String traceId = getTraceId(correlationId);
        try (MDC.MDCCloseable ignored = MDC.putCloseable("traceId", traceId)) {
            log.info("Processing deletePaymentInstruments request with correlationId: {}", correlationId);
            return visaClient.deletePaymentInstruments(request, traceId)
                    .doOnSuccess(response -> log.info("Successfully processed deletePaymentInstruments request"))
                    .doOnError(e -> log.error("Error processing deletePaymentInstruments request", e));
        }
    }

    @Queue("${rabbitmq.rpc.request-queue:visa-click-to-pay-requests}.getData")
    @ContinueSpan
    public Mono<GetDataResponse> getData(
            @SpanTag("getData.request") GetDataRequest request,
            @SpanTag("correlationId") String correlationId) {

        String traceId = getTraceId(correlationId);
        try (MDC.MDCCloseable ignored = MDC.putCloseable("traceId", traceId)) {
            log.info("Processing getData request with correlationId: {}", correlationId);
            return visaClient.getData(request, traceId)
                    .doOnSuccess(response -> log.info("Successfully processed getData request"))
                    .doOnError(e -> log.error("Error processing getData request", e));
        }
    }

    /**
     * Gets or generates a trace ID for logging and correlation.
     */
    private String getTraceId(String correlationId) {
        return correlationId != null ? correlationId : UUID.randomUUID().toString();
    }
}