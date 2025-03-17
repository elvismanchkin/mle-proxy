package dev.example.visa.client;

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
import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.retry.annotation.Retryable;
import reactor.core.publisher.Mono;

@Client("${visa.api.base-url}")
@Header(name = "Accept", value = MediaType.APPLICATION_JSON)
@Header(name = "Content-Type", value = MediaType.APPLICATION_JSON)
public interface VisaClickToPayClient {

    @Post("/visaIdCredential/v1/enrollPaymentInstruments")
    @Retryable(attempts = "${visa.api.retry.max-attempts:3}", delay = "${visa.api.retry.delay:1s}")
    Mono<RequestIdResponse> enrollPaymentInstruments(
            @Body EnrollPaymentInstrumentsRequest request,
            @Header("X-Correlation-Id") String correlationId);

    @Post("/visaIdCredential/v1/enrollData")
    @Retryable(attempts = "${visa.api.retry.max-attempts:3}", delay = "${visa.api.retry.delay:1s}")
    Mono<RequestIdResponse> enrollData(
            @Body EnrollDataRequest request,
            @Header("X-Correlation-Id") String correlationId);

    @Get("/visaIdCredential/v1/requestStatus/{requestTraceId}")
    @Retryable(attempts = "${visa.api.retry.max-attempts:3}", delay = "${visa.api.retry.delay:1s}")
    Mono<RequestStatusResponse> getRequestStatus(
            @NonNull @PathVariable String requestTraceId,
            @Header("X-Correlation-Id") String correlationId);

    @Put("/visaIdCredential/v1/managePaymentInstruments")
    @Retryable(attempts = "${visa.api.retry.max-attempts:3}", delay = "${visa.api.retry.delay:1s}")
    Mono<RequestIdResponse> managePaymentInstruments(
            @Body ManagePaymentInstrumentsRequest request,
            @Header("X-Correlation-Id") String correlationId);

    @Put("/visaIdCredential/v1/manageConsumerInformation")
    @Retryable(attempts = "${visa.api.retry.max-attempts:3}", delay = "${visa.api.retry.delay:1s}")
    Mono<RequestIdResponse> manageConsumerInformation(
            @Body ManageConsumerInformationRequest request,
            @Header("X-Correlation-Id") String correlationId);

    @Post("/visaIdCredential/v1/deleteConsumerInformation")
    @Retryable(attempts = "${visa.api.retry.max-attempts:3}", delay = "${visa.api.retry.delay:1s}")
    Mono<RequestIdResponse> deleteConsumerInformation(
            @Body DeleteConsumerInformationRequest request,
            @Header("X-Correlation-Id") String correlationId);

    @Post("/visaIdCredential/v1/deletePaymentInstruments")
    @Retryable(attempts = "${visa.api.retry.max-attempts:3}", delay = "${visa.api.retry.delay:1s}")
    Mono<RequestIdResponse> deletePaymentInstruments(
            @Body DeletePaymentInstrumentsRequest request,
            @Header("X-Correlation-Id") String correlationId);

    @Post("/visaIdCredential/v1/getData")
    @Retryable(attempts = "${visa.api.retry.max-attempts:3}", delay = "${visa.api.retry.delay:1s}")
    Mono<GetDataResponse> getData(
            @Body GetDataRequest request,
            @Header("X-Correlation-Id") String correlationId);
}