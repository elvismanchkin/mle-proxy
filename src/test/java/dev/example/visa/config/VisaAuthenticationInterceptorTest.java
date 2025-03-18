package dev.example.visa.config;

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

@Slf4j
@Singleton
@Requires(env = "test")
@Filter("${visa.api.base-url}/**")
public class VisaAuthenticationInterceptorTest implements HttpClientFilter {

    @Override
    @ContinueSpan
    public Publisher<? extends HttpResponse<?>> doFilter(
            @SpanTag("http.request") MutableHttpRequest<?> request,
            ClientFilterChain chain) {
        logRequest(request);

        return Mono.zip(
                Mono.just("some-api-key"),
                Mono.just("some-api-secret")
        ).flatMap(credentials -> {
            String apiKey = credentials.getT1();
            String apiSecret = credentials.getT2();
            String authHeader = createBasicAuthHeader(apiKey, apiSecret);

            request.header("Authorization", authHeader);
            request.header("X-Request-Timestamp", Instant.now().toString());

            log.debug("Added headers: Authorization={}, X-Request-Timestamp={}",
                    request.getHeaders().get("Authorization"),
                    request.getHeaders().get("X-Request-Timestamp"));

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
        log.debug("Sending request to Visa API: {} {}", request.getMethod(), request.getUri());

        request.getHeaders().forEach((name, values) -> {
            if (!name.equalsIgnoreCase("Authorization") && !name.equalsIgnoreCase("X-Client-Token")) {
                log.trace("Header: {} = {}", name, String.join(", ", values));
            }
        });
    }
}