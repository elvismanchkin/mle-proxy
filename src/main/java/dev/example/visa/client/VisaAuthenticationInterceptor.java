package dev.example.visa.client;

import dev.example.visa.security.VaultService;
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
@Filter("${visa.api.base-url}/**")
public class VisaAuthenticationInterceptor implements HttpClientFilter {

    private final VaultService vaultService;

    public VisaAuthenticationInterceptor(VaultService vaultService) {
        this.vaultService = vaultService;
    }

    @Override
    @ContinueSpan
    public Publisher<? extends HttpResponse<?>> doFilter(
            @SpanTag("http.request") MutableHttpRequest<?> request,
            ClientFilterChain chain) {

        // Add correlation ID if not present
        if (!request.getHeaders().contains("X-Correlation-Id")) {
            request.header("X-Correlation-Id", UUID.randomUUID().toString());
        }

        // Get API credentials and add Authorization header
        return Mono.zip(
                vaultService.getApiKey(),
                vaultService.getApiSecret()
        ).flatMap(credentials -> {
            String apiKey = credentials.getT1();
            String apiSecret = credentials.getT2();

            // Create Basic Authentication header
            String authHeader = createBasicAuthHeader(apiKey, apiSecret);
            request.header("Authorization", authHeader);

            // Add timestamp header (if needed by Visa API)
            request.header("X-Request-Timestamp", Instant.now().toString());

            // Log the request (mask sensitive data)
            logRequest(request);

            // Continue with the filter chain
            return Mono.from(chain.proceed(request));
        }).onErrorResume(e -> {
            log.error("Error in authentication interceptor", e);
            return Mono.error(e);
        });
    }

    private String createBasicAuthHeader(String username, String password) {
        String credentials = username + ":" + password;
        byte[] encodedCredentials = Base64.getEncoder().encode(credentials.getBytes(StandardCharsets.UTF_8));
        return "Basic " + new String(encodedCredentials, StandardCharsets.UTF_8);
    }

    private void logRequest(HttpRequest<?> request) {
        // Mask sensitive data in logs
        String maskedUri = request.getUri().toString();
        log.debug("Sending request to Visa API: {} {}", request.getMethod(), maskedUri);

        // Don't log headers with sensitive information
        request.getHeaders().forEach((name, values) -> {
            if (!name.equalsIgnoreCase("Authorization") && !name.equalsIgnoreCase("X-Client-Token")) {
                log.trace("Header: {} = {}", name, String.join(", ", values));
            }
        });
    }
}