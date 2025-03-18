package dev.example.visa.service;

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
import io.micronaut.tracing.annotation.NewSpan;
import io.micronaut.tracing.annotation.SpanTag;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Coordinates access to Visa Click to Pay operations.
 * Provides both direct API calls and asynchronous messaging with mapped responses.
 */
@Slf4j
@Singleton
public class VisaClickToPayCoordinator {

    private final VisaClickToPayService visaService;
    private final VisaClickToPayProducer visaProducer;

    public VisaClickToPayCoordinator(VisaClickToPayService visaService, VisaClickToPayProducer visaProducer) {
        this.visaService = visaService;
        this.visaProducer = visaProducer;
    }

    // Direct service methods with raw responses

    @NewSpan("visa.enrollData")
    public Mono<RequestIdResponse> enrollData(@SpanTag("request") EnrollDataRequest request) {
        String correlationId = generateCorrelationId();
        log.info("Enrolling consumer data with correlationId: {}", correlationId);
        return visaService.enrollData(request, correlationId);
    }

    @NewSpan("visa.enrollPaymentInstruments")
    public Mono<RequestIdResponse> enrollPaymentInstruments(@SpanTag("request") EnrollPaymentInstrumentsRequest request) {
        String correlationId = generateCorrelationId();
        log.info("Enrolling payment instruments with correlationId: {}", correlationId);
        return visaService.enrollPaymentInstruments(request, correlationId);
    }

    @NewSpan("visa.getRequestStatus")
    public Mono<dev.example.visa.model.RequestStatusResponse> getRequestStatus(@SpanTag("requestTraceId") String requestTraceId) {
        String correlationId = generateCorrelationId();
        log.info("Checking request status for requestTraceId: {} with correlationId: {}", requestTraceId, correlationId);
        return visaService.getRequestStatus(requestTraceId, correlationId);
    }

    @NewSpan("visa.managePaymentInstruments")
    public Mono<RequestIdResponse> managePaymentInstruments(@SpanTag("request") ManagePaymentInstrumentsRequest request) {
        String correlationId = generateCorrelationId();
        log.info("Managing payment instruments with correlationId: {}", correlationId);
        return visaService.managePaymentInstruments(request, correlationId);
    }

    @NewSpan("visa.manageConsumerInformation")
    public Mono<RequestIdResponse> manageConsumerInformation(@SpanTag("request") ManageConsumerInformationRequest request) {
        String correlationId = generateCorrelationId();
        log.info("Managing consumer information with correlationId: {}", correlationId);
        return visaService.manageConsumerInformation(request, correlationId);
    }

    @NewSpan("visa.deleteConsumerInformation")
    public Mono<RequestIdResponse> deleteConsumerInformation(@SpanTag("request") DeleteConsumerInformationRequest request) {
        String correlationId = generateCorrelationId();
        log.info("Deleting consumer information with correlationId: {}", correlationId);
        return visaService.deleteConsumerInformation(request, correlationId);
    }

    @NewSpan("visa.deletePaymentInstruments")
    public Mono<RequestIdResponse> deletePaymentInstruments(@SpanTag("request") DeletePaymentInstrumentsRequest request) {
        String correlationId = generateCorrelationId();
        log.info("Deleting payment instruments with correlationId: {}", correlationId);
        return visaService.deletePaymentInstruments(request, correlationId);
    }

    @NewSpan("visa.getData")
    public Mono<GetDataResponse> getData(@SpanTag("request") GetDataRequest request) {
        String correlationId = generateCorrelationId();
        log.info("Retrieving data with correlationId: {}", correlationId);
        return visaService.getData(request, correlationId);
    }

    // Direct service methods with mapped DTO responses

    @NewSpan("visa.enrollDataMapped")
    public Mono<EnrollmentResponseDto> enrollDataMapped(@SpanTag("request") EnrollDataRequest request) {
        String correlationId = generateCorrelationId();
        log.info("Enrolling consumer data (mapped) with correlationId: {}", correlationId);
        return visaService.enrollDataMapped(request, correlationId);
    }

    @NewSpan("visa.enrollPaymentInstrumentsMapped")
    public Mono<EnrollmentResponseDto> enrollPaymentInstrumentsMapped(@SpanTag("request") EnrollPaymentInstrumentsRequest request) {
        String correlationId = generateCorrelationId();
        log.info("Enrolling payment instruments (mapped) with correlationId: {}", correlationId);
        return visaService.enrollPaymentInstrumentsMapped(request, correlationId);
    }

    @NewSpan("visa.getRequestStatusMapped")
    public Mono<RequestStatusResponseDto> getRequestStatusMapped(@SpanTag("requestTraceId") String requestTraceId) {
        String correlationId = generateCorrelationId();
        log.info("Checking request status (mapped) for requestTraceId: {} with correlationId: {}", requestTraceId, correlationId);
        return visaService.getRequestStatusMapped(requestTraceId, correlationId);
    }

    @NewSpan("visa.managePaymentInstrumentsMapped")
    public Mono<EnrollmentResponseDto> managePaymentInstrumentsMapped(@SpanTag("request") ManagePaymentInstrumentsRequest request) {
        String correlationId = generateCorrelationId();
        log.info("Managing payment instruments (mapped) with correlationId: {}", correlationId);
        return visaService.managePaymentInstrumentsMapped(request, correlationId);
    }

    @NewSpan("visa.manageConsumerInformationMapped")
    public Mono<EnrollmentResponseDto> manageConsumerInformationMapped(@SpanTag("request") ManageConsumerInformationRequest request) {
        String correlationId = generateCorrelationId();
        log.info("Managing consumer information (mapped) with correlationId: {}", correlationId);
        return visaService.manageConsumerInformationMapped(request, correlationId);
    }

    @NewSpan("visa.deleteConsumerInformationMapped")
    public Mono<EnrollmentResponseDto> deleteConsumerInformationMapped(@SpanTag("request") DeleteConsumerInformationRequest request) {
        String correlationId = generateCorrelationId();
        log.info("Deleting consumer information (mapped) with correlationId: {}", correlationId);
        return visaService.deleteConsumerInformationMapped(request, correlationId);
    }

    @NewSpan("visa.deletePaymentInstrumentsMapped")
    public Mono<EnrollmentResponseDto> deletePaymentInstrumentsMapped(@SpanTag("request") DeletePaymentInstrumentsRequest request) {
        String correlationId = generateCorrelationId();
        log.info("Deleting payment instruments (mapped) with correlationId: {}", correlationId);
        return visaService.deletePaymentInstrumentsMapped(request, correlationId);
    }

    @NewSpan("visa.getDataMapped")
    public Mono<ConsumerDataResponseDto> getDataMapped(@SpanTag("request") GetDataRequest request) {
        String correlationId = generateCorrelationId();
        log.info("Retrieving data (mapped) with correlationId: {}", correlationId);
        return visaService.getDataMapped(request, correlationId);
    }

    // Async RabbitMQ methods (all return mapped DTOs)

    @NewSpan("visa.enrollDataAsync")
    public Mono<EnrollmentResponseDto> enrollDataAsync(@SpanTag("request") EnrollDataRequestDto request) {
        String correlationId = generateCorrelationId();
        log.info("Enrolling consumer data asynchronously with correlationId: {}", correlationId);
        return visaProducer.enrollData(request, correlationId);
    }

    @NewSpan("visa.enrollPaymentInstrumentsAsync")
    public Mono<EnrollmentResponseDto> enrollPaymentInstrumentsAsync(@SpanTag("request") EnrollPaymentInstrumentsRequestDto request) {
        String correlationId = generateCorrelationId();
        log.info("Enrolling payment instruments asynchronously with correlationId: {}", correlationId);
        return visaProducer.enrollPaymentInstruments(request, correlationId);
    }

    @NewSpan("visa.getRequestStatusAsync")
    public Mono<RequestStatusResponseDto> getRequestStatusAsync(@SpanTag("requestTraceId") String requestTraceId) {
        String correlationId = generateCorrelationId();
        log.info("Checking request status asynchronously for requestTraceId: {} with correlationId: {}", requestTraceId, correlationId);
        return visaProducer.requestStatus(requestTraceId, correlationId);
    }

    @NewSpan("visa.managePaymentInstrumentsAsync")
    public Mono<EnrollmentResponseDto> managePaymentInstrumentsAsync(@SpanTag("request") ManagePaymentInstrumentsRequestDto request) {
        String correlationId = generateCorrelationId();
        log.info("Managing payment instruments asynchronously with correlationId: {}", correlationId);
        return visaProducer.managePaymentInstruments(request, correlationId);
    }

    @NewSpan("visa.manageConsumerInformationAsync")
    public Mono<EnrollmentResponseDto> manageConsumerInformationAsync(@SpanTag("request") ManageConsumerInformationRequestDto request) {
        String correlationId = generateCorrelationId();
        log.info("Managing consumer information asynchronously with correlationId: {}", correlationId);
        return visaProducer.manageConsumerInformation(request, correlationId);
    }

    @NewSpan("visa.deleteConsumerInformationAsync")
    public Mono<EnrollmentResponseDto> deleteConsumerInformationAsync(@SpanTag("request") DeleteConsumerInformationRequestDto request) {
        String correlationId = generateCorrelationId();
        log.info("Deleting consumer information asynchronously with correlationId: {}", correlationId);
        return visaProducer.deleteConsumerInformation(request, correlationId);
    }

    @NewSpan("visa.deletePaymentInstrumentsAsync")
    public Mono<EnrollmentResponseDto> deletePaymentInstrumentsAsync(@SpanTag("request") DeletePaymentInstrumentsRequestDto request) {
        String correlationId = generateCorrelationId();
        log.info("Deleting payment instruments asynchronously with correlationId: {}", correlationId);
        return visaProducer.deletePaymentInstruments(request, correlationId);
    }

    @NewSpan("visa.getDataAsync")
    public Mono<ConsumerDataResponseDto> getDataAsync(@SpanTag("request") GetDataRequestDto request) {
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