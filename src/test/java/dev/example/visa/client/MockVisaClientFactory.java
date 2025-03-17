package dev.example.visa.client;

import dev.example.visa.model.ConsumerInformation;
import dev.example.visa.model.ConsumerInformationIdRef;
import dev.example.visa.model.DeleteConsumerInformationRequest;
import dev.example.visa.model.DeletePaymentInstrumentsRequest;
import dev.example.visa.model.EnrollDataRequest;
import dev.example.visa.model.EnrollPaymentInstrumentsRequest;
import dev.example.visa.model.GetDataRequest;
import dev.example.visa.model.GetDataResponse;
import dev.example.visa.model.Intent;
import dev.example.visa.model.ManageConsumerInformationRequest;
import dev.example.visa.model.ManagePaymentInstrumentsRequest;
import dev.example.visa.model.RequestIdResponse;
import dev.example.visa.model.RequestStatusResponse;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Factory that provides a mock implementation of the VisaClickToPayClient.
 * It simulates responses from the Visa API based on the Swagger specification.
 */
@Slf4j
@Factory
@Requires(env = "test")
public class MockVisaClientFactory {

    private final Map<String, RequestStatusResponse> requestStatusMap = new HashMap<>();
    private final Map<String, GetDataResponse.DataItem> consumerDataMap = new HashMap<>();

    @Singleton
    @Primary
    @Replaces(VisaClickToPayClient.class)
    public VisaClickToPayClient mockVisaClient() {
        log.info("Creating mock Visa Click to Pay client for tests");
        return new VisaClickToPayClient() {

            @Override
            public Mono<RequestIdResponse> enrollPaymentInstruments(EnrollPaymentInstrumentsRequest request, String correlationId) {
                String requestId = UUID.randomUUID().toString();
                setupRequestStatus(requestId, request.consumerInformation().externalConsumerID(), request.intent());
                log.debug("Mock: Enrolling payment instruments, generated requestId: {}", requestId);
                return Mono.just(new RequestIdResponse(requestId));
            }

            @Override
            public Mono<RequestIdResponse> enrollData(EnrollDataRequest request, String correlationId) {
                String requestId = UUID.randomUUID().toString();
                setupRequestStatus(requestId, request.consumerInformation().externalConsumerID(), request.intent());
                storeConsumerData(request);
                log.debug("Mock: Enrolling data, generated requestId: {}", requestId);
                return Mono.just(new RequestIdResponse(requestId));
            }

            @Override
            public Mono<RequestStatusResponse> getRequestStatus(String requestTraceId, String correlationId) {
                log.debug("Mock: Getting request status for: {}", requestTraceId);
                RequestStatusResponse status = requestStatusMap.getOrDefault(requestTraceId, createDefaultStatus());
                return Mono.just(status);
            }

            @Override
            public Mono<RequestIdResponse> managePaymentInstruments(ManagePaymentInstrumentsRequest request, String correlationId) {
                String requestId = UUID.randomUUID().toString();
                setupRequestStatus(requestId, request.consumerInformation().externalConsumerID(), request.intent());
                log.debug("Mock: Managing payment instruments, generated requestId: {}", requestId);
                return Mono.just(new RequestIdResponse(requestId));
            }

            @Override
            public Mono<RequestIdResponse> manageConsumerInformation(ManageConsumerInformationRequest request, String correlationId) {
                String requestId = UUID.randomUUID().toString();
                setupRequestStatus(requestId, request.consumerInformation().externalConsumerID(), request.intent());
                log.debug("Mock: Managing consumer information, generated requestId: {}", requestId);
                return Mono.just(new RequestIdResponse(requestId));
            }

            @Override
            public Mono<RequestIdResponse> deleteConsumerInformation(DeleteConsumerInformationRequest request, String correlationId) {
                String requestId = UUID.randomUUID().toString();
                setupRequestStatus(requestId, request.consumerInformation().externalConsumerID(), request.intent());
                log.debug("Mock: Deleting consumer information, generated requestId: {}", requestId);
                return Mono.just(new RequestIdResponse(requestId));
            }

            @Override
            public Mono<RequestIdResponse> deletePaymentInstruments(DeletePaymentInstrumentsRequest request, String correlationId) {
                String requestId = UUID.randomUUID().toString();
                setupRequestStatus(requestId, request.consumerInformation().externalConsumerID(), request.intent());
                log.debug("Mock: Deleting payment instruments, generated requestId: {}", requestId);
                return Mono.just(new RequestIdResponse(requestId));
            }

            @Override
            public Mono<GetDataResponse> getData(GetDataRequest request, String correlationId) {
                log.debug("Mock: Getting data for consumer: {}", request.consumerInformation().externalConsumerID());

                // Build a response based on the stored consumer data or create a default one
                List<GetDataResponse.DataItem> dataItems = new ArrayList<>();

                String consumerId = request.consumerInformation().externalConsumerID();
                GetDataResponse.DataItem dataItem = consumerDataMap.get(consumerId);

                if (dataItem != null) {
                    dataItems.add(dataItem);
                } else {
                    // Create default data if not found
                    dataItems.add(createDefaultDataItem(request));
                }

                return Mono.just(new GetDataResponse(dataItems));
            }
        };
    }

    // Helper methods

    private void setupRequestStatus(String requestId, String consumerId, Intent intent) {
        RequestStatusResponse.StatusDetail detail = RequestStatusResponse.StatusDetail.builder()
                .intent(intent)
                .status("SUCCESS")
                .build();

        RequestStatusResponse status = RequestStatusResponse.builder()
                .status("COMPLETED")
                .details(Collections.singletonList(detail))
                .consumerInformation(new ConsumerInformationIdRef(consumerId))
                .build();

        requestStatusMap.put(requestId, status);
    }

    private void storeConsumerData(EnrollDataRequest request) {
        GetDataResponse.DataItem dataItem = GetDataResponse.DataItem.builder()
                .intent(request.intent())
                .consumerInformation(request.consumerInformation())
                .paymentInstruments(request.paymentInstruments())
                .build();

        consumerDataMap.put(request.consumerInformation().externalConsumerID(), dataItem);
    }

    private RequestStatusResponse createDefaultStatus() {
        return RequestStatusResponse.builder()
                .status("COMPLETED")
                .details(Collections.emptyList())
                .build();
    }

    private GetDataResponse.DataItem createDefaultDataItem(GetDataRequest request) {
        return GetDataResponse.DataItem.builder()
                .intent(request.intent())
                .consumerInformation(ConsumerInformation.builder()
                        .firstName("Test")
                        .lastName("User")
                        .countryCode("USA")
                        .externalConsumerID(request.consumerInformation().externalConsumerID())
                        .build())
                .paymentInstruments(Collections.emptyList())
                .build();
    }
}