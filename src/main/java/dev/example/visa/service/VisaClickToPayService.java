package dev.example.visa.service;

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
import io.micronaut.tracing.annotation.NewSpan;
import io.micronaut.tracing.annotation.SpanTag;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Service layer for Visa Click to Pay operations.
 * This service coordinates between the HTTP client and the messaging layer.
 */
@Slf4j
@Singleton
public class VisaClickToPayService {

    private final VisaClickToPayClient visaClient;

    public VisaClickToPayService(VisaClickToPayClient visaClient) {
        this.visaClient = visaClient;
    }

    /**
     * Enrolls consumer information and payment instruments.
     *
     * @param request The enrollment request
     * @param correlationId Optional correlation ID for tracing
     * @return A Mono containing the request ID response
     */
    @NewSpan("visa.service.enrollData")
    public Mono<RequestIdResponse> enrollData(
            @SpanTag("request") EnrollDataRequest request,
            @SpanTag("correlationId") String correlationId) {

        String traceId = getTraceId(correlationId);
        try (MDC.MDCCloseable ignored = MDC.putCloseable("traceId", traceId)) {
            log.info("Enrolling consumer data with correlationId: {}", traceId);
            return visaClient.enrollData(request, traceId)
                    .doOnSuccess(response -> log.info("Successfully enrolled consumer data, requestTraceId: {}",
                            response.requestTraceId()))
                    .doOnError(e -> log.error("Error enrolling consumer data", e));
        }
    }

    /**
     * Enrolls payment instruments for an existing consumer.
     *
     * @param request The payment instrument enrollment request
     * @param correlationId Optional correlation ID for tracing
     * @return A Mono containing the request ID response
     */
    @NewSpan("visa.service.enrollPaymentInstruments")
    public Mono<RequestIdResponse> enrollPaymentInstruments(
            @SpanTag("request") EnrollPaymentInstrumentsRequest request,
            @SpanTag("correlationId") String correlationId) {

        String traceId = getTraceId(correlationId);
        try (MDC.MDCCloseable ignored = MDC.putCloseable("traceId", traceId)) {
            log.info("Enrolling payment instruments with correlationId: {}", traceId);
            return visaClient.enrollPaymentInstruments(request, traceId)
                    .doOnSuccess(response -> log.info("Successfully enrolled payment instruments, requestTraceId: {}",
                            response.requestTraceId()))
                    .doOnError(e -> log.error("Error enrolling payment instruments", e));
        }
    }

    /**
     * Retrieves the status of a previously submitted request.
     *
     * @param requestTraceId The ID of the request to check
     * @param correlationId Optional correlation ID for tracing
     * @return A Mono containing the request status response
     */
    @NewSpan("visa.service.getRequestStatus")
    public Mono<RequestStatusResponse> getRequestStatus(
            @SpanTag("requestTraceId") String requestTraceId,
            @SpanTag("correlationId") String correlationId) {

        String traceId = getTraceId(correlationId);
        try (MDC.MDCCloseable ignored = MDC.putCloseable("traceId", traceId)) {
            log.info("Checking request status for requestTraceId: {} with correlationId: {}",
                    requestTraceId, traceId);
            return visaClient.getRequestStatus(requestTraceId, traceId)
                    .doOnSuccess(response -> log.info("Retrieved request status: {}", response.status()))
                    .doOnError(e -> log.error("Error retrieving request status", e));
        }
    }

    /**
     * Updates payment instruments for an existing consumer.
     *
     * @param request The payment instrument update request
     * @param correlationId Optional correlation ID for tracing
     * @return A Mono containing the request ID response
     */
    @NewSpan("visa.service.managePaymentInstruments")
    public Mono<RequestIdResponse> managePaymentInstruments(
            @SpanTag("request") ManagePaymentInstrumentsRequest request,
            @SpanTag("correlationId") String correlationId) {

        String traceId = getTraceId(correlationId);
        try (MDC.MDCCloseable ignored = MDC.putCloseable("traceId", traceId)) {
            log.info("Managing payment instruments with correlationId: {}", traceId);
            return visaClient.managePaymentInstruments(request, traceId)
                    .doOnSuccess(response -> log.info("Successfully managed payment instruments, requestTraceId: {}",
                            response.requestTraceId()))
                    .doOnError(e -> log.error("Error managing payment instruments", e));
        }
    }

    /**
     * Updates consumer information.
     *
     * @param request The consumer information update request
     * @param correlationId Optional correlation ID for tracing
     * @return A Mono containing the request ID response
     */
    @NewSpan("visa.service.manageConsumerInformation")
    public Mono<RequestIdResponse> manageConsumerInformation(
            @SpanTag("request") ManageConsumerInformationRequest request,
            @SpanTag("correlationId") String correlationId) {

        String traceId = getTraceId(correlationId);
        try (MDC.MDCCloseable ignored = MDC.putCloseable("traceId", traceId)) {
            log.info("Managing consumer information with correlationId: {}", traceId);
            return visaClient.manageConsumerInformation(request, traceId)
                    .doOnSuccess(response -> log.info("Successfully managed consumer information, requestTraceId: {}",
                            response.requestTraceId()))
                    .doOnError(e -> log.error("Error managing consumer information", e));
        }
    }

    /**
     * Deletes consumer information.
     *
     * @param request The consumer information deletion request
     * @param correlationId Optional correlation ID for tracing
     * @return A Mono containing the request ID response
     */
    @NewSpan("visa.service.deleteConsumerInformation")
    public Mono<RequestIdResponse> deleteConsumerInformation(
            @SpanTag("request") DeleteConsumerInformationRequest request,
            @SpanTag("correlationId") String correlationId) {

        String traceId = getTraceId(correlationId);
        try (MDC.MDCCloseable ignored = MDC.putCloseable("traceId", traceId)) {
            log.info("Deleting consumer information with correlationId: {}", traceId);
            return visaClient.deleteConsumerInformation(request, traceId)
                    .doOnSuccess(response -> log.info("Successfully deleted consumer information, requestTraceId: {}",
                            response.requestTraceId()))
                    .doOnError(e -> log.error("Error deleting consumer information", e));
        }
    }

    /**
     * Deletes payment instruments.
     *
     * @param request The payment instrument deletion request
     * @param correlationId Optional correlation ID for tracing
     * @return A Mono containing the request ID response
     */
    @NewSpan("visa.service.deletePaymentInstruments")
    public Mono<RequestIdResponse> deletePaymentInstruments(
            @SpanTag("request") DeletePaymentInstrumentsRequest request,
            @SpanTag("correlationId") String correlationId) {

        String traceId = getTraceId(correlationId);
        try (MDC.MDCCloseable ignored = MDC.putCloseable("traceId", traceId)) {
            log.info("Deleting payment instruments with correlationId: {}", traceId);
            return visaClient.deletePaymentInstruments(request, traceId)
                    .doOnSuccess(response -> log.info("Successfully deleted payment instruments, requestTraceId: {}",
                            response.requestTraceId()))
                    .doOnError(e -> log.error("Error deleting payment instruments", e));
        }
    }

    /**
     * Retrieves consumer and payment data.
     *
     * @param request The data retrieval request
     * @param correlationId Optional correlation ID for tracing
     * @return A Mono containing the response with consumer and payment data
     */
    @NewSpan("visa.service.getData")
    public Mono<GetDataResponse> getData(
            @SpanTag("request") GetDataRequest request,
            @SpanTag("correlationId") String correlationId) {

        String traceId = getTraceId(correlationId);
        try (MDC.MDCCloseable ignored = MDC.putCloseable("traceId", traceId)) {
            log.info("Retrieving data with correlationId: {}", traceId);
            return visaClient.getData(request, traceId)
                    .doOnSuccess(response -> log.info("Successfully retrieved data"))
                    .doOnError(e -> log.error("Error retrieving data", e));
        }
    }

    /**
     * Gets or generates a trace ID for logging and correlation.
     */
    private String getTraceId(String correlationId) {
        return correlationId != null ? correlationId : UUID.randomUUID().toString();
    }
}