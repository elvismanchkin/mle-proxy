package dev.example.visa.service;

import dev.example.visa.messaging.VisaClickToPayProducer;
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
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Coordinates access to Visa Click to Pay operations.
 * Provides both direct API calls and asynchronous messaging.
 */
@Slf4j
@Singleton
public class VisaClickToPayCoordinator {

    private final VisaClickToPayService visaService;
    private final VisaClickToPayProducer visaProducer;

    /**
     * Constructor that takes both direct service and RabbitMQ producer.
     * This allows clients to choose either direct API calls or async messaging.
     */
    public VisaClickToPayCoordinator(VisaClickToPayService visaService, VisaClickToPayProducer visaProducer) {
        this.visaService = visaService;
        this.visaProducer = visaProducer;
    }

    /**
     * Enrolls a consumer with payment information synchronously.
     */
    @NewSpan("visa.enrollData")
    public Mono<RequestIdResponse> enrollData(@SpanTag("request") EnrollDataRequest request) {
        String correlationId = generateCorrelationId();
        log.info("Enrolling consumer data with correlationId: {}", correlationId);
        return visaService.enrollData(request, correlationId);
    }

    /**
     * Enrolls a consumer with payment information asynchronously via RabbitMQ.
     */
    @NewSpan("visa.enrollDataAsync")
    public Mono<RequestIdResponse> enrollDataAsync(@SpanTag("request") EnrollDataRequest request) {
        String correlationId = generateCorrelationId();
        log.info("Enrolling consumer data asynchronously with correlationId: {}", correlationId);
        return visaProducer.enrollData(request, correlationId);
    }

    /**
     * Enrolls payment instruments for an existing consumer synchronously.
     */
    @NewSpan("visa.enrollPaymentInstruments")
    public Mono<RequestIdResponse> enrollPaymentInstruments(
            @SpanTag("request") EnrollPaymentInstrumentsRequest request) {
        String correlationId = generateCorrelationId();
        log.info("Enrolling payment instruments with correlationId: {}", correlationId);
        return visaService.enrollPaymentInstruments(request, correlationId);
    }

    /**
     * Enrolls payment instruments for an existing consumer asynchronously via RabbitMQ.
     */
    @NewSpan("visa.enrollPaymentInstrumentsAsync")
    public Mono<RequestIdResponse> enrollPaymentInstrumentsAsync(
            @SpanTag("request") EnrollPaymentInstrumentsRequest request) {
        String correlationId = generateCorrelationId();
        log.info("Enrolling payment instruments asynchronously with correlationId: {}", correlationId);
        return visaProducer.enrollPaymentInstruments(request, correlationId);
    }

    /**
     * Retrieves the status of a previously submitted request synchronously.
     */
    @NewSpan("visa.getRequestStatus")
    public Mono<RequestStatusResponse> getRequestStatus(@SpanTag("requestTraceId") String requestTraceId) {
        String correlationId = generateCorrelationId();
        log.info("Checking request status for requestTraceId: {} with correlationId: {}",
                requestTraceId, correlationId);
        return visaService.getRequestStatus(requestTraceId, correlationId);
    }

    /**
     * Retrieves the status of a previously submitted request asynchronously via RabbitMQ.
     */
    @NewSpan("visa.getRequestStatusAsync")
    public Mono<RequestStatusResponse> getRequestStatusAsync(@SpanTag("requestTraceId") String requestTraceId) {
        String correlationId = generateCorrelationId();
        log.info("Checking request status asynchronously for requestTraceId: {} with correlationId: {}",
                requestTraceId, correlationId);
        return visaProducer.requestStatus(requestTraceId, correlationId);
    }

    /**
     * Updates payment instruments for an existing consumer synchronously.
     */
    @NewSpan("visa.managePaymentInstruments")
    public Mono<RequestIdResponse> managePaymentInstruments(
            @SpanTag("request") ManagePaymentInstrumentsRequest request) {
        String correlationId = generateCorrelationId();
        log.info("Managing payment instruments with correlationId: {}", correlationId);
        return visaService.managePaymentInstruments(request, correlationId);
    }

    /**
     * Updates payment instruments for an existing consumer asynchronously via RabbitMQ.
     */
    @NewSpan("visa.managePaymentInstrumentsAsync")
    public Mono<RequestIdResponse> managePaymentInstrumentsAsync(
            @SpanTag("request") ManagePaymentInstrumentsRequest request) {
        String correlationId = generateCorrelationId();
        log.info("Managing payment instruments asynchronously with correlationId: {}", correlationId);
        return visaProducer.managePaymentInstruments(request, correlationId);
    }

    /**
     * Updates consumer information synchronously.
     */
    @NewSpan("visa.manageConsumerInformation")
    public Mono<RequestIdResponse> manageConsumerInformation(
            @SpanTag("request") ManageConsumerInformationRequest request) {
        String correlationId = generateCorrelationId();
        log.info("Managing consumer information with correlationId: {}", correlationId);
        return visaService.manageConsumerInformation(request, correlationId);
    }

    /**
     * Updates consumer information asynchronously via RabbitMQ.
     */
    @NewSpan("visa.manageConsumerInformationAsync")
    public Mono<RequestIdResponse> manageConsumerInformationAsync(
            @SpanTag("request") ManageConsumerInformationRequest request) {
        String correlationId = generateCorrelationId();
        log.info("Managing consumer information asynchronously with correlationId: {}", correlationId);
        return visaProducer.manageConsumerInformation(request, correlationId);
    }

    /**
     * Deletes consumer information synchronously.
     */
    @NewSpan("visa.deleteConsumerInformation")
    public Mono<RequestIdResponse> deleteConsumerInformation(
            @SpanTag("request") DeleteConsumerInformationRequest request) {
        String correlationId = generateCorrelationId();
        log.info("Deleting consumer information with correlationId: {}", correlationId);
        return visaService.deleteConsumerInformation(request, correlationId);
    }

    /**
     * Deletes consumer information asynchronously via RabbitMQ.
     */
    @NewSpan("visa.deleteConsumerInformationAsync")
    public Mono<RequestIdResponse> deleteConsumerInformationAsync(
            @SpanTag("request") DeleteConsumerInformationRequest request) {
        String correlationId = generateCorrelationId();
        log.info("Deleting consumer information asynchronously with correlationId: {}", correlationId);
        return visaProducer.deleteConsumerInformation(request, correlationId);
    }

    /**
     * Deletes payment instruments synchronously.
     */
    @NewSpan("visa.deletePaymentInstruments")
    public Mono<RequestIdResponse> deletePaymentInstruments(
            @SpanTag("request") DeletePaymentInstrumentsRequest request) {
        String correlationId = generateCorrelationId();
        log.info("Deleting payment instruments with correlationId: {}", correlationId);
        return visaService.deletePaymentInstruments(request, correlationId);
    }

    /**
     * Deletes payment instruments asynchronously via RabbitMQ.
     */
    @NewSpan("visa.deletePaymentInstrumentsAsync")
    public Mono<RequestIdResponse> deletePaymentInstrumentsAsync(
            @SpanTag("request") DeletePaymentInstrumentsRequest request) {
        String correlationId = generateCorrelationId();
        log.info("Deleting payment instruments asynchronously with correlationId: {}", correlationId);
        return visaProducer.deletePaymentInstruments(request, correlationId);
    }

    /**
     * Retrieves consumer and payment data synchronously.
     */
    @NewSpan("visa.getData")
    public Mono<GetDataResponse> getData(@SpanTag("request") GetDataRequest request) {
        String correlationId = generateCorrelationId();
        log.info("Retrieving data with correlationId: {}", correlationId);
        return visaService.getData(request, correlationId);
    }

    /**
     * Retrieves consumer and payment data asynchronously via RabbitMQ.
     */
    @NewSpan("visa.getDataAsync")
    public Mono<GetDataResponse> getDataAsync(@SpanTag("request") GetDataRequest request) {
        String correlationId = generateCorrelationId();
        log.info("Retrieving data asynchronously with correlationId: {}", correlationId);
        return visaProducer.getData(request, correlationId);
    }

    /**
     * Generates a unique correlation ID for tracing.
     */
    private String generateCorrelationId() {
        return UUID.randomUUID().toString();
    }
}