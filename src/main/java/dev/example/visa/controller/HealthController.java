package dev.example.visa.controller;

import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.health.HealthStatus;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.runtime.context.scope.Refreshable;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Simple health check controller.
 * This endpoint can be used by monitoring systems to check the health of the service.
 */
@Slf4j
@Controller("/health")
@Refreshable
public class HealthController {

    /**
     * Returns the health status of the service.
     *
     * @return A HTTP response with the health details
     */
    @Get
    @Produces(MediaType.APPLICATION_JSON)
    @ExecuteOn(TaskExecutors.IO)
    @SingleResult
    public Publisher<HttpResponse<Map<String, Object>>> health() {
        log.debug("Health check endpoint called");

        Map<String, Object> details = new LinkedHashMap<>();
        details.put("status", HealthStatus.UP.toString());
        details.put("timestamp", Instant.now().toString());
        details.put("version", getBuildVersion());
        details.put("serviceName", "visa-click-to-pay-proxy");

        return Mono.just(HttpResponse.ok(details));
    }

    /**
     * Returns the version of the service from the package or a default value.
     *
     * @return The version string
     */
    private String getBuildVersion() {
        Package pkg = getClass().getPackage();
        String version = pkg != null ? pkg.getImplementationVersion() : null;
        return version != null ? version : "0.1";
    }
}