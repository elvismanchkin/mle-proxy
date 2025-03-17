package dev.example.visa.messaging;

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
import io.micronaut.rabbitmq.annotation.Binding;
import io.micronaut.rabbitmq.annotation.RabbitClient;
import io.micronaut.rabbitmq.annotation.RabbitProperty;
import io.micronaut.tracing.annotation.NewSpan;
import io.micronaut.tracing.annotation.SpanTag;
import reactor.core.publisher.Mono;

/**
 * RabbitMQ client for sending requests to the Visa Click to Pay service.
 * This interface defines methods that map to the Visa Click to Pay API operations.
 * Each method sends a message to a specific routing key and returns a Mono with the response.
 */
@RabbitClient("${rabbitmq.exchange.name:visa-click-to-pay-exchange}")
public interface VisaClickToPayProducer {

    @NewSpan("visa.enrollData")
    @Binding("enrollData")
    Mono<RequestIdResponse> enrollData(
            @SpanTag("request") EnrollDataRequest request,
            @RabbitProperty("correlationId") String correlationId);

    @NewSpan("visa.enrollPaymentInstruments")
    @Binding("enrollPaymentInstruments")
    Mono<RequestIdResponse> enrollPaymentInstruments(
            @SpanTag("request") EnrollPaymentInstrumentsRequest request,
            @RabbitProperty("correlationId") String correlationId);

    @NewSpan("visa.requestStatus")
    @Binding("requestStatus")
    Mono<RequestStatusResponse> requestStatus(
            @SpanTag("requestTraceId") String requestTraceId,
            @RabbitProperty("correlationId") String correlationId);

    @NewSpan("visa.managePaymentInstruments")
    @Binding("managePaymentInstruments")
    Mono<RequestIdResponse> managePaymentInstruments(
            @SpanTag("request") ManagePaymentInstrumentsRequest request,
            @RabbitProperty("correlationId") String correlationId);

    @NewSpan("visa.manageConsumerInformation")
    @Binding("manageConsumerInformation")
    Mono<RequestIdResponse> manageConsumerInformation(
            @SpanTag("request") ManageConsumerInformationRequest request,
            @RabbitProperty("correlationId") String correlationId);

    @NewSpan("visa.deleteConsumerInformation")
    @Binding("deleteConsumerInformation")
    Mono<RequestIdResponse> deleteConsumerInformation(
            @SpanTag("request") DeleteConsumerInformationRequest request,
            @RabbitProperty("correlationId") String correlationId);

    @NewSpan("visa.deletePaymentInstruments")
    @Binding("deletePaymentInstruments")
    Mono<RequestIdResponse> deletePaymentInstruments(
            @SpanTag("request") DeletePaymentInstrumentsRequest request,
            @RabbitProperty("correlationId") String correlationId);

    @NewSpan("visa.getData")
    @Binding("getData")
    Mono<GetDataResponse> getData(
            @SpanTag("request") GetDataRequest request,
            @RabbitProperty("correlationId") String correlationId);
}