package dev.example.visa.service;

import dev.example.visa.client.VisaClickToPayClient;
import dev.example.visa.dto.ConsumerDataResponseDto;
import dev.example.visa.dto.EnrollmentResponseDto;
import dev.example.visa.dto.RequestStatusResponseDto;
import dev.example.visa.mappers.VisaMapper;
import dev.example.visa.model.DeleteConsumerInformationRequest;
import dev.example.visa.model.DeletePaymentInstrumentsRequest;
import dev.example.visa.model.EnrollDataRequest;
import dev.example.visa.model.EnrollPaymentInstrumentsRequest;
import dev.example.visa.model.ErrorResponse;
import dev.example.visa.model.GetDataRequest;
import dev.example.visa.model.GetDataResponse;
import dev.example.visa.model.ManageConsumerInformationRequest;
import dev.example.visa.model.ManagePaymentInstrumentsRequest;
import dev.example.visa.model.RequestIdResponse;
import dev.example.visa.model.RequestStatusResponse;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.tracing.annotation.NewSpan;
import io.micronaut.tracing.annotation.SpanTag;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Enhanced service that provides both raw API responses and mapped DTOs.
 */
@Slf4j
@Singleton
public class VisaClickToPayService {

    private final VisaClickToPayClient visaClient;
    private final VisaMapper visaMapper;

    public VisaClickToPayService(VisaClickToPayClient visaClient, VisaMapper visaMapper) {
        this.visaClient = visaClient;
        this.visaMapper = visaMapper;
    }

    // Raw API methods
    @NewSpan("visa.service.enrollData")
    public Mono<RequestIdResponse> enrollData(@SpanTag("request") EnrollDataRequest request, @SpanTag("correlationId") String correlationId) {

        String traceId = getTraceId(correlationId);
        try (MDC.MDCCloseable ignored = MDC.putCloseable("traceId", traceId)) {
            log.info("Enrolling consumer data with correlationId: {}", traceId);
            return visaClient.enrollData(request, traceId)
                    .doOnSuccess(response -> log.info("Successfully enrolled consumer data, requestTraceId: {}", response.requestTraceId()))
                    .doOnError(e -> log.error("Error enrolling consumer data", e));
        }
    }

    @NewSpan("visa.service.enrollPaymentInstruments")
    public Mono<RequestIdResponse> enrollPaymentInstruments(@SpanTag("request") EnrollPaymentInstrumentsRequest request, @SpanTag("correlationId") String correlationId) {

        String traceId = getTraceId(correlationId);
        try (MDC.MDCCloseable ignored = MDC.putCloseable("traceId", traceId)) {
            log.info("Enrolling payment instruments with correlationId: {}", traceId);
            return visaClient.enrollPaymentInstruments(request, traceId)
                    .doOnSuccess(response -> log.info("Successfully enrolled payment instruments, requestTraceId: {}", response.requestTraceId()))
                    .doOnError(e -> log.error("Error enrolling payment instruments", e));
        }
    }

    @NewSpan("visa.service.getRequestStatus")
    public Mono<RequestStatusResponse> getRequestStatus(@SpanTag("requestTraceId") String requestTraceId, @SpanTag("correlationId") String correlationId) {

        String traceId = getTraceId(correlationId);
        try (MDC.MDCCloseable ignored = MDC.putCloseable("traceId", traceId)) {
            log.info("Checking request status for requestTraceId: {} with correlationId: {}", requestTraceId, traceId);
            return visaClient.getRequestStatus(requestTraceId, traceId)
                    .doOnSuccess(response -> log.info("Retrieved request status: {}", response.status()))
                    .doOnError(e -> log.error("Error retrieving request status", e));
        }
    }

    @NewSpan("visa.service.managePaymentInstruments")
    public Mono<RequestIdResponse> managePaymentInstruments(@SpanTag("request") ManagePaymentInstrumentsRequest request, @SpanTag("correlationId") String correlationId) {

        String traceId = getTraceId(correlationId);
        try (MDC.MDCCloseable ignored = MDC.putCloseable("traceId", traceId)) {
            log.info("Managing payment instruments with correlationId: {}", traceId);
            return visaClient.managePaymentInstruments(request, traceId)
                    .doOnSuccess(response -> log.info("Successfully managed payment instruments, requestTraceId: {}", response.requestTraceId()))
                    .doOnError(e -> log.error("Error managing payment instruments", e));
        }
    }

    @NewSpan("visa.service.manageConsumerInformation")
    public Mono<RequestIdResponse> manageConsumerInformation(@SpanTag("request") ManageConsumerInformationRequest request, @SpanTag("correlationId") String correlationId) {

        String traceId = getTraceId(correlationId);
        try (MDC.MDCCloseable ignored = MDC.putCloseable("traceId", traceId)) {
            log.info("Managing consumer information with correlationId: {}", traceId);
            return visaClient.manageConsumerInformation(request, traceId)
                    .doOnSuccess(response -> log.info("Successfully managed consumer information, requestTraceId: {}", response.requestTraceId()))
                    .doOnError(e -> log.error("Error managing consumer information", e));
        }
    }

    @NewSpan("visa.service.deleteConsumerInformation")
    public Mono<RequestIdResponse> deleteConsumerInformation(@SpanTag("request") DeleteConsumerInformationRequest request, @SpanTag("correlationId") String correlationId) {

        String traceId = getTraceId(correlationId);
        try (MDC.MDCCloseable ignored = MDC.putCloseable("traceId", traceId)) {
            log.info("Deleting consumer information with correlationId: {}", traceId);
            return visaClient.deleteConsumerInformation(request, traceId)
                    .doOnSuccess(response -> log.info("Successfully deleted consumer information, requestTraceId: {}", response.requestTraceId()))
                    .doOnError(e -> log.error("Error deleting consumer information", e));
        }
    }

    @NewSpan("visa.service.deletePaymentInstruments")
    public Mono<RequestIdResponse> deletePaymentInstruments(@SpanTag("request") DeletePaymentInstrumentsRequest request, @SpanTag("correlationId") String correlationId) {

        String traceId = getTraceId(correlationId);
        try (MDC.MDCCloseable ignored = MDC.putCloseable("traceId", traceId)) {
            log.info("Deleting payment instruments with correlationId: {}", traceId);
            return visaClient.deletePaymentInstruments(request, traceId)
                    .doOnSuccess(response -> log.info("Successfully deleted payment instruments, requestTraceId: {}", response.requestTraceId()))
                    .doOnError(e -> log.error("Error deleting payment instruments", e));
        }
    }

    @NewSpan("visa.service.getData")
    public Mono<GetDataResponse> getData(@SpanTag("request") GetDataRequest request, @SpanTag("correlationId") String correlationId) {

        String traceId = getTraceId(correlationId);
        try (MDC.MDCCloseable ignored = MDC.putCloseable("traceId", traceId)) {
            log.info("Retrieving data with correlationId: {}", traceId);
            return visaClient.getData(request, traceId)
                    .doOnSuccess(response -> log.info("Successfully retrieved data"))
                    .doOnError(e -> log.error("Error retrieving data", e));
        }
    }

    // Mapped API methods - these return the flattened DTOs

    @NewSpan("visa.service.enrollDataMapped")
    public Mono<EnrollmentResponseDto> enrollDataMapped(@SpanTag("request") EnrollDataRequest request, @SpanTag("correlationId") String correlationId) {
        return enrollData(request, correlationId)
                .map(visaMapper::mapToEnrollmentResponse)
                .onErrorResume(HttpClientResponseException.class, e -> {
                    ErrorResponse errorResponse = e.getResponse().getBody(ErrorResponse.class)
                            .orElseGet(() -> ErrorResponse.builder()
                                    .reason("ClientError")
                                    .message(e.getMessage())
                                    .build());
                    return Mono.just(visaMapper.createErrorEnrollmentResponse(errorResponse));
                });
    }

    @NewSpan("visa.service.enrollPaymentInstrumentsMapped")
    public Mono<EnrollmentResponseDto> enrollPaymentInstrumentsMapped(@SpanTag("request") EnrollPaymentInstrumentsRequest request, @SpanTag("correlationId") String correlationId) {
        return enrollPaymentInstruments(request, correlationId)
                .map(visaMapper::mapToEnrollmentResponse)
                .onErrorResume(HttpClientResponseException.class, e -> {
                    ErrorResponse errorResponse = e.getResponse().getBody(ErrorResponse.class)
                            .orElseGet(() -> ErrorResponse.builder()
                                    .reason("ClientError")
                                    .message(e.getMessage())
                                    .build());
                    return Mono.just(visaMapper.createErrorEnrollmentResponse(errorResponse));
                });
    }

    @NewSpan("visa.service.getRequestStatusMapped")
    public Mono<RequestStatusResponseDto> getRequestStatusMapped(@SpanTag("requestTraceId") String requestTraceId, @SpanTag("correlationId") String correlationId) {
        return getRequestStatus(requestTraceId, correlationId)
                .map(visaMapper::mapToRequestStatusResponse)
                .onErrorResume(HttpClientResponseException.class, e -> {
                    ErrorResponse errorResponse = e.getResponse().getBody(ErrorResponse.class)
                            .orElseGet(() -> ErrorResponse.builder()
                                    .reason("ClientError")
                                    .message(e.getMessage())
                                    .build());
                    return Mono.just(visaMapper.createErrorStatusResponse(errorResponse));
                });
    }

    @NewSpan("visa.service.managePaymentInstrumentsMapped")
    public Mono<EnrollmentResponseDto> managePaymentInstrumentsMapped(@SpanTag("request") ManagePaymentInstrumentsRequest request, @SpanTag("correlationId") String correlationId) {
        return managePaymentInstruments(request, correlationId)
                .map(visaMapper::mapToManagementResponse)
                .onErrorResume(HttpClientResponseException.class, e -> {
                    ErrorResponse errorResponse = e.getResponse().getBody(ErrorResponse.class)
                            .orElseGet(() -> ErrorResponse.builder()
                                    .reason("ClientError")
                                    .message(e.getMessage())
                                    .build());
                    return Mono.just(visaMapper.createErrorEnrollmentResponse(errorResponse));
                });
    }

    @NewSpan("visa.service.manageConsumerInformationMapped")
    public Mono<EnrollmentResponseDto> manageConsumerInformationMapped(@SpanTag("request") ManageConsumerInformationRequest request, @SpanTag("correlationId") String correlationId) {
        return manageConsumerInformation(request, correlationId)
                .map(visaMapper::mapToManagementResponse)
                .onErrorResume(HttpClientResponseException.class, e -> {
                    ErrorResponse errorResponse = e.getResponse().getBody(ErrorResponse.class)
                            .orElseGet(() -> ErrorResponse.builder()
                                    .reason("ClientError")
                                    .message(e.getMessage())
                                    .build());
                    return Mono.just(visaMapper.createErrorEnrollmentResponse(errorResponse));
                });
    }

    @NewSpan("visa.service.deleteConsumerInformationMapped")
    public Mono<EnrollmentResponseDto> deleteConsumerInformationMapped(@SpanTag("request") DeleteConsumerInformationRequest request, @SpanTag("correlationId") String correlationId) {
        return deleteConsumerInformation(request, correlationId)
                .map(visaMapper::mapToDeletionResponse)
                .onErrorResume(HttpClientResponseException.class, e -> {
                    ErrorResponse errorResponse = e.getResponse().getBody(ErrorResponse.class)
                            .orElseGet(() -> ErrorResponse.builder()
                                    .reason("ClientError")
                                    .message(e.getMessage())
                                    .build());
                    return Mono.just(visaMapper.createErrorEnrollmentResponse(errorResponse));
                });
    }

    @NewSpan("visa.service.deletePaymentInstrumentsMapped")
    public Mono<EnrollmentResponseDto> deletePaymentInstrumentsMapped(@SpanTag("request") DeletePaymentInstrumentsRequest request, @SpanTag("correlationId") String correlationId) {
        return deletePaymentInstruments(request, correlationId)
                .map(visaMapper::mapToDeletionResponse)
                .onErrorResume(HttpClientResponseException.class, e -> {
                    ErrorResponse errorResponse = e.getResponse().getBody(ErrorResponse.class)
                            .orElseGet(() -> ErrorResponse.builder()
                                    .reason("ClientError")
                                    .message(e.getMessage())
                                    .build());
                    return Mono.just(visaMapper.createErrorEnrollmentResponse(errorResponse));
                });
    }

    @NewSpan("visa.service.getDataMapped")
    public Mono<ConsumerDataResponseDto> getDataMapped(@SpanTag("request") GetDataRequest request, @SpanTag("correlationId") String correlationId) {
        return getData(request, correlationId)
                .map(visaMapper::mapToConsumerDataResponse)
                .onErrorResume(HttpClientResponseException.class, e -> {
                    ErrorResponse errorResponse = e.getResponse().getBody(ErrorResponse.class)
                            .orElseGet(() -> ErrorResponse.builder()
                                    .reason("ClientError")
                                    .message(e.getMessage())
                                    .build());
                    return Mono.just(visaMapper.createErrorDataResponse(errorResponse));
                });
    }

    /**
     * Gets or generates a trace ID for logging and correlation.
     */
    private String getTraceId(String correlationId) {
        return correlationId != null ? correlationId : UUID.randomUUID().toString();
    }
}