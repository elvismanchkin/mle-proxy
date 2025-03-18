package dev.example.visa.client;

import dev.example.visa.security.VaultService;
import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.ClientFilterChain;
import io.micronaut.http.filter.HttpClientFilter;
import io.micronaut.tracing.annotation.ContinueSpan;
import io.micronaut.tracing.annotation.SpanTag;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Slf4j
@Singleton
@Requires(notEnv = "test")
@Filter("${visa.api.base-url}/**")
public class VisaAuthenticationInterceptor implements HttpClientFilter {

    private final VaultService vaultService;

    public VisaAuthenticationInterceptor(VaultService vaultService) {
        this.vaultService = vaultService;
    }

    @Override
    @ContinueSpan
    public Publisher<? extends HttpResponse<?>> doFilter(
            @SpanTag("http.request") MutableHttpRequest<?> request, ClientFilterChain chain) {

        if (!request.getHeaders().contains("X-Correlation-Id")) {
            request.header("X-Correlation-Id", UUID.randomUUID().toString());
        }

        return Mono.zip(vaultService.getApiKey(), vaultService.getApiSecret())
                .flatMap(credentials -> {
                    String apiKey = credentials.getT1();
                    String apiSecret = credentials.getT2();

                    request.header("Authorization", createBasicAuthHeader(apiKey, apiSecret));
                    request.header("X-Request-Timestamp", Instant.now().toString());

                    logRequest(request);

                    return Mono.from(chain.proceed(request));
                })
                .onErrorResume(e -> {
                    log.error("Error in authentication interceptor", e);
                    return Mono.error(e);
                });
    }

    private String createBasicAuthHeader(String username, String password) {
        String credentials = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }

    private void logRequest(HttpRequest<?> request) {
        log.debug("Sending request to Visa API: {} {}", request.getMethod(), request.getUri());

        request.getHeaders().forEach((name, values) -> {
            if (!"Authorization".equalsIgnoreCase(name) && !"X-Client-Token".equalsIgnoreCase(name)) {
                log.trace("Header: {} = {}", name, String.join(", ", values));
            }
        });
    }
}