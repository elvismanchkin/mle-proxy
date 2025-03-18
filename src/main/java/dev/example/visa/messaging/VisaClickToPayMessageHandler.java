package dev.example.visa.messaging;

import dev.example.visa.dto.*;
import dev.example.visa.mappers.VisaRequestMapper;
import dev.example.visa.service.VisaClickToPayService;
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
 * Enhanced RabbitMQ message handler that handles flattened DTOs.
 */
@Slf4j
@Singleton
@RabbitListener
public class VisaClickToPayMessageHandler {

    private final VisaClickToPayService visaService;
    private final VisaRequestMapper requestMapper;

    @Value("${rabbitmq.rpc.request-queue:visa-click-to-pay-requests}")
    private String requestQueuePrefix;

    public VisaClickToPayMessageHandler(VisaClickToPayService visaService, VisaRequestMapper requestMapper) {
        this.visaService = visaService;
        this.requestMapper = requestMapper;
    }

    @Queue("${rabbitmq.rpc.request-queue:visa-click-to-pay-requests}.enrollData")
    @ContinueSpan
    public Mono<EnrollmentResponseDto> enrollData(
            @SpanTag("enrollData.request") EnrollDataRequestDto requestDto,
            @SpanTag("correlationId") String correlationId) {

        String traceId = getTraceId(correlationId);
        try (MDC.MDCCloseable ignored = MDC.putCloseable("traceId", traceId)) {
            log.info("Processing enrollData request with correlationId: {}", correlationId);
            return visaService.enrollDataMapped(
                            requestMapper.mapToEnrollDataRequest(requestDto), traceId)
                    .doOnSuccess(response -> log.info("Successfully processed enrollData request: {}",
                            response.status()))
                    .doOnError(e -> log.error("Error processing enrollData request", e));
        }
    }

    @Queue("${rabbitmq.rpc.request-queue:visa-click-to-pay-requests}.enrollPaymentInstruments")
    @ContinueSpan
    public Mono<EnrollmentResponseDto> enrollPaymentInstruments(
            @SpanTag("enrollPaymentInstruments.request") EnrollPaymentInstrumentsRequestDto requestDto,
            @SpanTag("correlationId") String correlationId) {

        String traceId = getTraceId(correlationId);
        try (MDC.MDCCloseable ignored = MDC.putCloseable("traceId", traceId)) {
            log.info("Processing enrollPaymentInstruments request with correlationId: {}", correlationId);
            return visaService.enrollPaymentInstrumentsMapped(
                            requestMapper.mapToEnrollPaymentInstrumentsRequest(requestDto), traceId)
                    .doOnSuccess(response -> log.info("Successfully processed enrollPaymentInstruments request: {}",
                            response.status()))
                    .doOnError(e -> log.error("Error processing enrollPaymentInstruments request", e));
        }
    }

    @Queue("${rabbitmq.rpc.request-queue:visa-click-to-pay-requests}.requestStatus")
    @ContinueSpan
    public Mono<RequestStatusResponseDto> requestStatus(
            @SpanTag("requestStatus.requestTraceId") String requestTraceId,
            @SpanTag("correlationId") String correlationId) {

        String traceId = getTraceId(correlationId);
        try (MDC.MDCCloseable ignored = MDC.putCloseable("traceId", traceId)) {
            log.info("Processing requestStatus for requestTraceId: {} with correlationId: {}",
                    requestTraceId, correlationId);
            return visaService.getRequestStatusMapped(requestTraceId, traceId)
                    .doOnSuccess(response -> log.info("Successfully retrieved request status: {}",
                            response.status()))
                    .doOnError(e -> log.error("Error retrieving request status", e));
        }
    }

    @Queue("${rabbitmq.rpc.request-queue:visa-click-to-pay-requests}.managePaymentInstruments")
    @ContinueSpan
    public Mono<EnrollmentResponseDto> managePaymentInstruments(
            @SpanTag("managePaymentInstruments.request") ManagePaymentInstrumentsRequestDto requestDto,
            @SpanTag("correlationId") String correlationId) {

        String traceId = getTraceId(correlationId);
        try (MDC.MDCCloseable ignored = MDC.putCloseable("traceId", traceId)) {
            log.info("Processing managePaymentInstruments request with correlationId: {}", correlationId);
            return visaService.managePaymentInstrumentsMapped(
                            requestMapper.mapToManagePaymentInstrumentsRequest(requestDto), traceId)
                    .doOnSuccess(response -> log.info("Successfully processed managePaymentInstruments request: {}",
                            response.status()))
                    .doOnError(e -> log.error("Error processing managePaymentInstruments request", e));
        }
    }

    @Queue("${rabbitmq.rpc.request-queue:visa-click-to-pay-requests}.manageConsumerInformation")
    @ContinueSpan
    public Mono<EnrollmentResponseDto> manageConsumerInformation(
            @SpanTag("manageConsumerInformation.request") ManageConsumerInformationRequestDto requestDto,
            @SpanTag("correlationId") String correlationId) {

        String traceId = getTraceId(correlationId);
        try (MDC.MDCCloseable ignored = MDC.putCloseable("traceId", traceId)) {
            log.info("Processing manageConsumerInformation request with correlationId: {}", correlationId);
            return visaService.manageConsumerInformationMapped(
                            requestMapper.mapToManageConsumerInformationRequest(requestDto), traceId)
                    .doOnSuccess(response -> log.info("Successfully processed manageConsumerInformation request: {}",
                            response.status()))
                    .doOnError(e -> log.error("Error processing manageConsumerInformation request", e));
        }
    }

    @Queue("${rabbitmq.rpc.request-queue:visa-click-to-pay-requests}.deleteConsumerInformation")
    @ContinueSpan
    public Mono<EnrollmentResponseDto> deleteConsumerInformation(
            @SpanTag("deleteConsumerInformation.request") DeleteConsumerInformationRequestDto requestDto,
            @SpanTag("correlationId") String correlationId) {

        String traceId = getTraceId(correlationId);
        try (MDC.MDCCloseable ignored = MDC.putCloseable("traceId", traceId)) {
            log.info("Processing deleteConsumerInformation request with correlationId: {}", correlationId);
            return visaService.deleteConsumerInformationMapped(
                            requestMapper.mapToDeleteConsumerInformationRequest(requestDto), traceId)
                    .doOnSuccess(response -> log.info("Successfully processed deleteConsumerInformation request: {}",
                            response.status()))
                    .doOnError(e -> log.error("Error processing deleteConsumerInformation request", e));
        }
    }

    @Queue("${rabbitmq.rpc.request-queue:visa-click-to-pay-requests}.deletePaymentInstruments")
    @ContinueSpan
    public Mono<EnrollmentResponseDto> deletePaymentInstruments(
            @SpanTag("deletePaymentInstruments.request") DeletePaymentInstrumentsRequestDto requestDto,
            @SpanTag("correlationId") String correlationId) {

        String traceId = getTraceId(correlationId);
        try (MDC.MDCCloseable ignored = MDC.putCloseable("traceId", traceId)) {
            log.info("Processing deletePaymentInstruments request with correlationId: {}", correlationId);
            return visaService.deletePaymentInstrumentsMapped(
                            requestMapper.mapToDeletePaymentInstrumentsRequest(requestDto), traceId)
                    .doOnSuccess(response -> log.info("Successfully processed deletePaymentInstruments request: {}",
                            response.status()))
                    .doOnError(e -> log.error("Error processing deletePaymentInstruments request", e));
        }
    }

    @Queue("${rabbitmq.rpc.request-queue:visa-click-to-pay-requests}.getData")
    @ContinueSpan
    public Mono<ConsumerDataResponseDto> getData(
            @SpanTag("getData.request") GetDataRequestDto requestDto,
            @SpanTag("correlationId") String correlationId) {

        String traceId = getTraceId(correlationId);
        try (MDC.MDCCloseable ignored = MDC.putCloseable("traceId", traceId)) {
            log.info("Processing getData request with correlationId: {}", correlationId);
            return visaService.getDataMapped(
                            requestMapper.mapToGetDataRequest(requestDto), traceId)
                    .doOnSuccess(response -> log.info("Successfully processed getData request: {}",
                            response.status()))
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