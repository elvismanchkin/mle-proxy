package dev.example.visa.service;

import dev.example.visa.client.VisaClickToPayClient;
import dev.example.visa.model.EnrollDataRequest;
import dev.example.visa.model.RequestIdResponse;
import dev.example.visa.model.RequestStatusResponse;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@MicronautTest
class VisaClickToPayServiceV2Test {

    @Inject
    VisaClickToPayService service;

    @Inject
    VisaClickToPayClient visaClient;

    @Test
    void enrollDataTest() {
        EnrollDataRequest request = new EnrollDataRequest(null, List.of(), null);
        RequestIdResponse expected = new RequestIdResponse("dummyTraceId");
        when(visaClient.enrollData(eq(request), anyString())).thenReturn(Mono.just(expected));

        Mono<RequestIdResponse> result = service.enrollData(request, "corr-123");
        StepVerifier.create(result)
                .expectNextMatches(r -> "dummyTraceId".equals(r.requestTraceId()))
                .verifyComplete();

        verify(visaClient).enrollData(eq(request), anyString());
    }

    @Test
    void getRequestStatusTest() {
        RequestStatusResponse expected = new RequestStatusResponse("status-ok", null,null);
        when(visaClient.getRequestStatus(eq("req-123"), anyString())).thenReturn(Mono.just(expected));

        Mono<RequestStatusResponse> result = service.getRequestStatus("req-123", null);
        StepVerifier.create(result)
                .expectNextMatches(r -> "status-ok".equals(r.status()))
                .verifyComplete();

        verify(visaClient).getRequestStatus(eq("req-123"), anyString());
    }

    // Additional tests for other methods can be implemented similarly.

    @io.micronaut.context.annotation.Factory
    static class Mocks {
        @io.micronaut.context.annotation.Bean
        VisaClickToPayClient visaClickToPayClient() {
            return mock(VisaClickToPayClient.class);
        }
    }
}