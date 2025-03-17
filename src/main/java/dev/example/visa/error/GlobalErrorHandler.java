package dev.example.visa.error;

import dev.example.visa.model.ErrorResponse;
import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Singleton
@Produces
@Requires(classes = {HttpClientResponseException.class, ExceptionHandler.class})
public class GlobalErrorHandler implements ExceptionHandler<Throwable, HttpResponse<?>> {

    @Override
    public HttpResponse<?> handle(HttpRequest request, Throwable exception) {
        log.error("Error handling request: {}", request.getUri(), exception);

        if (exception instanceof HttpClientResponseException clientException) {
            return handleHttpClientException(clientException);
        }

        ThrowableProblem problem = Problem.builder()
                .withType(URI.create("https://api.example.dev/errors/server-error"))
                .withTitle("Internal Server Error")
                .withStatus(Status.INTERNAL_SERVER_ERROR)
                .withDetail(exception.getMessage())
                .withInstance(URI.create(request.getUri().toString()))
                .build();

        return HttpResponse.serverError(problem);
    }

    private HttpResponse<?> handleHttpClientException(HttpClientResponseException exception) {
        HttpStatus status = exception.getStatus();

        Optional<ErrorResponse> visaErrorOpt = exception.getResponse()
                .getBody(ErrorResponse.class);

        if (visaErrorOpt.isPresent()) {
            ErrorResponse visaError = visaErrorOpt.get();

            List<ErrorResponse.ErrorDetail> details = visaError.details() != null
                    ? visaError.details()
                    : Collections.emptyList();

            ThrowableProblem problem = Problem.builder()
                    .withType(URI.create("https://api.example.dev/errors/visa-api-error"))
                    .withTitle(visaError.reason() != null ? visaError.reason() : "Visa API Error")
                    .withStatus(mapToZalandoStatus(status))
                    .withDetail(visaError.message())
                    .with("details", details)
                    .build();

            return HttpResponse.status(status).body(problem);
        }

        ThrowableProblem problem = Problem.builder()
                .withType(URI.create("https://api.example.dev/errors/api-error"))
                .withTitle("API Communication Error")
                .withStatus(mapToZalandoStatus(status))
                .withDetail(exception.getMessage())
                .build();

        return HttpResponse.status(status).body(problem);
    }

    private Status mapToZalandoStatus(HttpStatus status) {
        return switch (status.getCode()) {
            case 400 -> Status.BAD_REQUEST;
            case 401 -> Status.UNAUTHORIZED;
            case 403 -> Status.FORBIDDEN;
            case 404 -> Status.NOT_FOUND;
            case 405 -> Status.METHOD_NOT_ALLOWED;
            case 409 -> Status.CONFLICT;
            case 422 -> Status.UNPROCESSABLE_ENTITY;
            case 429 -> Status.TOO_MANY_REQUESTS;
            case 500 -> Status.INTERNAL_SERVER_ERROR;
            case 502 -> Status.BAD_GATEWAY;
            case 503 -> Status.SERVICE_UNAVAILABLE;
            case 504 -> Status.GATEWAY_TIMEOUT;
            default -> Status.INTERNAL_SERVER_ERROR;
        };
    }
}