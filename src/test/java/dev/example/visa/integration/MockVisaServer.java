package dev.example.visa.integration;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Produces;
import io.micronaut.runtime.server.EmbeddedServer;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * A mock server that simulates Visa API endpoints for testing.
 * This server allows dynamic configuration of mock responses for different endpoints.
 */
@Singleton
@Controller
@Requires(env = "test")
@Property(name = "rabbitmq.enabled", value = "false")
public class MockVisaServer {
    private static final Logger LOG = LoggerFactory.getLogger(MockVisaServer.class);

    // Store endpoint configurations
    private final Map<String, EndpointConfig> endpointConfigs = new ConcurrentHashMap<>();

    // Store captured requests for inspection
    private final Map<String, HttpRequest<?>> capturedRequests = new ConcurrentHashMap<>();

    /**
     * Setup a mock endpoint with a specific response.
     *
     * @param config The endpoint configuration
     * @return HTTP response
     */
    @Post("/mock-setup")
    public HttpResponse<?> setupMockEndpoint(@Body EndpointConfig config) {
        LOG.info("Setting up mock endpoint: {}", config.path);
        endpointConfigs.put(config.path, config);
        return HttpResponse.ok();
    }

    /**
     * Setup request capturing for a specific path.
     *
     * @param config The capture configuration
     * @return HTTP response
     */
    @Post("/mock-capture")
    public HttpResponse<?> setupRequestCapture(@Body CaptureConfig config) {
        LOG.info("Setting up request capture for path: {}", config.path);
        // Setup a basic endpoint config that will capture the request
        EndpointConfig endpointConfig = new EndpointConfig();
        endpointConfig.path = config.path;
        endpointConfig.status = 200;
        endpointConfig.response = Collections.emptyMap();
        endpointConfig.captureRequest = true;

        endpointConfigs.put(config.path, endpointConfig);
        return HttpResponse.ok();
    }

    /**
     * Get the last captured request.
     *
     * @return The captured request data
     */
    @Get("/mock-captured-request")
    public HttpResponse<?> getCapturedRequest() {
        if (capturedRequests.isEmpty()) {
            return HttpResponse.notFound();
        }

        // Return the first captured request (in a real implementation, you might want to
        // support multiple captures and retrieval by path)
        String path = capturedRequests.keySet().iterator().next();
        HttpRequest<?> request = capturedRequests.get(path);

        // Convert the request to a simplified map for inspection
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("path", path);
        requestData.put("method", request.getMethod().toString());

        // Extract headers into a map
        Map<String, String> headers = new HashMap<>();
        request.getHeaders().forEach(header ->
                headers.put(header.getKey(), header.getValue().getFirst()));
        requestData.put("headers", headers);

        // Include body if available
        request.getBody().ifPresent(body -> requestData.put("body", body));

        return HttpResponse.ok(requestData);
    }

    /**
     * Catch-all handler for Visa API endpoints.
     *
     * @param request The HTTP request
     * @return The configured mock response
     */
    @Post(value = "/visaIdCredential/v1/{endpoint}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<?> handleVisaApiPost(HttpRequest<?> request, @PathVariable String endpoint) {
        return handleVisaApiRequest(request, "/visaIdCredential/v1/" + endpoint);
    }

    /**
     * Catch-all handler for Visa API GET endpoints.
     *
     * @param request The HTTP request
     * @return The configured mock response
     */
    @Get(value = "/visaIdCredential/v1/{endpoint}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<?> handleVisaApiGet(HttpRequest<?> request, @PathVariable String endpoint) {
        return handleVisaApiRequest(request, "/visaIdCredential/v1/" + endpoint);
    }

    /**
     * Catch-all handler for Visa API GET endpoints with path variables.
     *
     * @param request The HTTP request
     * @return The configured mock response
     */
    @Get(value = "/visaIdCredential/v1/{endpoint}/{pathVar}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<?> handleVisaApiGetWithPathVar(
            HttpRequest<?> request,
            @PathVariable String endpoint,
            @PathVariable String pathVar) {
        return handleVisaApiRequest(request, "/visaIdCredential/v1/" + endpoint + "/" + pathVar);
    }

    /**
     * Common handler logic for all Visa API requests.
     *
     * @param request The HTTP request
     * @param path The request path
     * @return The configured mock response
     */
    private HttpResponse<?> handleVisaApiRequest(HttpRequest<?> request, String path) {
        LOG.info("Received request for mock Visa API: {}", path);

        // Try to find exact path match first
        EndpointConfig config = endpointConfigs.get(path);

        // If no exact match, try regex matching
        if (config == null) {
            config = findRegexMatch(path);
        }

        // If still no match, return 404
        if (config == null) {
            LOG.warn("No mock configuration found for path: {}", path);
            return HttpResponse.notFound();
        }

        // Capture the request if configured
        if (config.captureRequest) {
            capturedRequests.put(path, request);
        }

        // Return the configured response
        return HttpResponse.status(HttpStatus.valueOf(config.status))
                .body(config.response);
    }

    /**
     * Find a regex pattern match for a path.
     *
     * @param path The path to match
     * @return The matching endpoint configuration, or null if none
     */
    private EndpointConfig findRegexMatch(String path) {
        for (Map.Entry<String, EndpointConfig> entry : endpointConfigs.entrySet()) {
            if (Boolean.TRUE.equals(entry.getValue().isRegex)) {
                if (Pattern.matches(entry.getKey(), path)) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    /**
     * Configuration for a mock endpoint.
     */
    public static class EndpointConfig {
        public String path;
        public int status = 200;
        public Object response;
        public Boolean isRegex = false;
        public Boolean captureRequest = false;
    }

    /**
     * Configuration for request capturing.
     */
    public static class CaptureConfig {
        public String path;
    }

    /**
     * Helper method to start a mock server and get a client for it.
     *
     * @return A map containing the server and client
     */
    public static Map<String, Object> startMockServer() {
        EmbeddedServer server = ApplicationContext.run(EmbeddedServer.class,
                Collections.singletonMap("micronaut.server.port", -1));

        return Map.of(
                "server", server,
                "client", server.getApplicationContext()
                        .createBean(io.micronaut.http.client.HttpClient.class, server.getURL())
        );
    }
}