package dev.example.visa.messaging;

import dev.example.visa.dto.*;
import io.micronaut.rabbitmq.annotation.Binding;
import io.micronaut.rabbitmq.annotation.RabbitClient;
import io.micronaut.rabbitmq.annotation.RabbitProperty;
import io.micronaut.tracing.annotation.NewSpan;
import io.micronaut.tracing.annotation.SpanTag;
import reactor.core.publisher.Mono;

/**
 * RabbitMQ producer interface using flattened DTO requests and responses.
 */
@RabbitClient("${rabbitmq.exchange.name:visa-click-to-pay-exchange}")
public interface VisaClickToPayProducer {

    @NewSpan("visa.enrollData")
    @Binding("enrollData")
    Mono<EnrollmentResponseDto> enrollData(
            @SpanTag("request") EnrollDataRequestDto request,
            @RabbitProperty("correlationId") String correlationId);

    @NewSpan("visa.enrollPaymentInstruments")
    @Binding("enrollPaymentInstruments")
    Mono<EnrollmentResponseDto> enrollPaymentInstruments(
            @SpanTag("request") EnrollPaymentInstrumentsRequestDto request,
            @RabbitProperty("correlationId") String correlationId);

    @NewSpan("visa.requestStatus")
    @Binding("requestStatus")
    Mono<RequestStatusResponseDto> requestStatus(
            @SpanTag("requestTraceId") String requestTraceId,
            @RabbitProperty("correlationId") String correlationId);

    @NewSpan("visa.managePaymentInstruments")
    @Binding("managePaymentInstruments")
    Mono<EnrollmentResponseDto> managePaymentInstruments(
            @SpanTag("request") ManagePaymentInstrumentsRequestDto request,
            @RabbitProperty("correlationId") String correlationId);

    @NewSpan("visa.manageConsumerInformation")
    @Binding("manageConsumerInformation")
    Mono<EnrollmentResponseDto> manageConsumerInformation(
            @SpanTag("request") ManageConsumerInformationRequestDto request,
            @RabbitProperty("correlationId") String correlationId);

    @NewSpan("visa.deleteConsumerInformation")
    @Binding("deleteConsumerInformation")
    Mono<EnrollmentResponseDto> deleteConsumerInformation(
            @SpanTag("request") DeleteConsumerInformationRequestDto request,
            @RabbitProperty("correlationId") String correlationId);

    @NewSpan("visa.deletePaymentInstruments")
    @Binding("deletePaymentInstruments")
    Mono<EnrollmentResponseDto> deletePaymentInstruments(
            @SpanTag("request") DeletePaymentInstrumentsRequestDto request,
            @RabbitProperty("correlationId") String correlationId);

    @NewSpan("visa.getData")
    @Binding("getData")
    Mono<ConsumerDataResponseDto> getData(
            @SpanTag("request") GetDataRequestDto request,
            @RabbitProperty("correlationId") String correlationId);
}