package dev.example.visa.security;

import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.StartupEvent;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Mock implementation of VaultService for testing.
 * Returns predefined values instead of interacting with an actual Vault server.
 */
@Slf4j
@Singleton
@Primary
@Replaces(VaultService.class)
@Requires(env = "test")
public class MockVaultService extends VaultService {

    private static final String MOCK_CERTIFICATE = "-----BEGIN CERTIFICATE-----\n" +
            "MIIDazCCAlOgAwIBAgIUOjuGFd9HUtKnCiB/xhGUIBJ5vgIwDQYJKoZIhvcNAQEL\n" +
            "BQAwRTELMAkGA1UEBhMCQVUxEzARBgNVBAgMClNvbWUtU3RhdGUxITAfBgNVBAoM\n" +
            "GEludGVybmV0IFdpZGdpdHMgUHR5IEx0ZDAeFw0yNTAzMTgxMzI4MTlaFw0yNjAz\n" +
            "MTgxMzI4MTlaMEUxCzAJBgNVBAYTAkFVMRMwEQYDVQQIDApTb21lLVN0YXRlMSEw\n" +
            "HwYDVQQKDBhJbnRlcm5ldCBXaWRnaXRzIFB0eSBMdGQwggEiMA0GCSqGSIb3DQEB\n" +
            "AQUAA4IBDwAwggEKAoIBAQD0JrXwdYSWE7QTD4sDuRFWEOA3w5o3I1+dfxfV4FOq\n" +
            "YvvDsYPMfxNcMKTbzS0LHGJ+XcVgI3UP7xjM8QdLwCVZF0S5nLeMnkIH5MlHSHzk\n" +
            "gGXlEUcYLpMO9xsHVx0dxCCFL5bGlqHVG+WrQSCMNh6QwhqP3MOYoT5fQIFfbhic\n" +
            "iu3qHEa3a46Kfwxs6kRcmJIJ3TlSIyuDsUlZNVI06gy7cIerIZVQRWNWWn2+X9jE\n" +
            "M2KZl11NEKPWx8CYN5TMckEu7OUFT9fIHLgQVIFDtGxAp0zZnEGnfXviQL2Y6ASb\n" +
            "BHhiK7qZQT2IlKyD3/UDx1TBjwIvGmKlUCkVxZ9BAgMBAAGjUzBRMB0GA1UdDgQW\n" +
            "BBQ0cs5UMF7n7JMDgJX3hFtZB2uwTDAfBgNVHSMEGDAWgBQ0cs5UMF7n7JMDgJX3\n" +
            "hFtZB2uwTDAPBgNVHRMBAf8EBTADAQH/MA0GCSqGSIb3DQEBCwUAA4IBAQBS+Mah\n" +
            "4Jxvh9GRJl/7AuDG9sGEv4XKE7/S7IAUgg1Gj6g+Y8QAkY2XfrwY9pBdnBKC29Lw\n" +
            "e+0V8I5DBqb1u/7L5PgXGR2R2l9CxVxILTaqG3pjFQKOEG/4JKXLGjZKXyYzOiVE\n" +
            "uJnXPZwVXRoqtAb2438q/clDOUqaIY6JwfxEoWtQOHAHuVHtLBWv1lAXyQdN4QEb\n" +
            "uGH8uaTX6CyR/FBa2YLGhDb4YCQvF9+EKZo5/e1MhHiCvZcCrRZ/U82REBpKYFnH\n" +
            "RYe5cE9RbMJRQiTLsvooBOlEUCFlfzM3gmOoTIpKBMqwCKpzmJzCgMiz9UIhAsmG\n" +
            "JLJ8ZCcUvKS5sDBV\n" +
            "-----END CERTIFICATE-----";

    private static final String MOCK_PRIVATE_KEY = "-----BEGIN PRIVATE KEY-----\n" +
            "MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQD0JrXwdYSWE7QT\n" +
            "D4sDuRFWEOA3w5o3I1+dfxfV4FOqYvvDsYPMfxNcMKTbzS0LHGJ+XcVgI3UP7xjM\n" +
            "8QdLwCVZF0S5nLeMnkIH5MlHSHzkgGXlEUcYLpMO9xsHVx0dxCCFL5bGlqHVG+Wr\n" +
            "QSCMNh6QwhqP3MOYoT5fQIFfbhiciu3qHEa3a46Kfwxs6kRcmJIJ3TlSIyuDsUlZ\n" +
            "NVI06gy7cIerIZVQRWNWWn2+X9jEM2KZl11NEKPWx8CYN5TMckEu7OUFT9fIHLgQ\n" +
            "VIFDtGxAp0zZnEGnfXviQL2Y6ASbBHhiK7qZQT2IlKyD3/UDx1TBjwIvGmKlUCkV\n" +
            "xZ9BAgMBAAECggEBAM4JfAg5UZFCwSRvSKXXrB8jGwjxHDDnuGTvZzCXyjANnJJF\n" +
            "lKzC+YWt+g7fMV1GFSvnz+5yQn1UcsuYM3EjZFGwZuYp1j9cqPHiLeNTpRxJDLI+\n" +
            "eNVLGe6kd5JYa4at5lkU5/xJFWiIY0HKs6vU0XN5R2KvzeejNYFa5a0TXX1fYUPV\n" +
            "RUH5NuiwfQ0KcX6GLf7GTsoHXgFkGVNzr6USXbl9tnkaU5JSfSWZHqV/bUFj5EOZ\n" +
            "JhfcVdC8nCJDlI1yQTjJQA5Z9EBPI93B9jC8nIkBxd5tGWdUXrJnLE3NNV/t6hzs\n" +
            "9MWKnAqQwmCPZDd6XYB/mSS1CHAuYX4BWxF6bk5NqAECgYEA+iTGXq9JeQ32Yj6r\n" +
            "NvvI4pHQoGJ+ixi5K/MwqYiYFJnCRsrSvKFSyHRjJxUNv/Mc6mDKNu10K2N9+7F5\n" +
            "k+QaxnCC2LaYGOGNc2M+YrJPFvwxn3oIGVXBuCEfHvB/+e4M+Cj5P6iIbwaXjmqH\n" +
            "IjMcPsEUQzv3d+XRawtRXaBZlsECgYEA+cLgXKXW5wfIigwCYIG/kkMU4LZgCOhA\n" +
            "tNnkOvDPKhXVfX6qx4XORpWwHWS+b4sIzisRZE2P5FCUCNsRRrBMm1mE8a+TZg0l\n" +
            "dO5XwOkKFlALvldhx6ZQ0j+CBKa9UmqY0Mh07Qn8O5bLblGpVdBLbQxPDLZdLGXF\n" +
            "HZgQJd38OcECgYA+o6ek+zgU9HHRjyvX+FAM8nAyAHcwS/Iilb9L4BJ87CXxhyaQ\n" +
            "vdO58QA5Sl+hPxBVj7wu/5jTD4DG8wF2JKbGkaPP8LpQ5ALwjmr0J6NNLlm9mX0y\n" +
            "3rd5sP4iIDrAMhxXiTFKnww8eMXUFQuTvWQXZ3cCPnb0sS5M8VkHoDV4wQKBgQDu\n" +
            "9kRagarPQ9KWmfDVKkuMlmQWH8y3zLusJPAXe3wXrDOBdqgpEHe0CvzWI5Ied84G\n" +
            "zKnmahXicBK4A3IarBhtR3OMwJN/RE/bMMJIvykvq21WZP0VQABUXzQkIHMdGsJW\n" +
            "yvKGZcFJjGPcVt3at4hQv5RvPfr/vnLRkbRXICf3AQKBgQDOhXKiU9rDNFJB97FY\n" +
            "1jKcXjHJwHYQP16VmOFCuyXBj4QlOGnYfT/BlMYoB8QNeD8qfqwYVFxSFXnD4Z5p\n" +
            "LT9z+CX0Z9Q/lX4kwB4uVIhqs0bXM1nNZ13QXl88BhPocrPjjzNUXLQ6Kv1+1JOT\n" +
            "Q7iRiSV75l7+2qTwdEoWUr6b9g==\n" +
            "-----END PRIVATE KEY-----";

    private static final String MOCK_API_KEY = "test_api_key_123";
    private static final String MOCK_API_SECRET = "test_api_secret_456";

    @Override
    public void onApplicationEvent(StartupEvent event) {
        log.info("Mock Vault Service initialized");
        // No-op - don't try to connect to Vault
    }

    @Override
    public Mono<String> getClientCertificate() {
        log.debug("Returning mock client certificate");
        return Mono.just(MOCK_CERTIFICATE);
    }

    @Override
    public Mono<String> getClientKey() {
        log.debug("Returning mock client key");
        return Mono.just(MOCK_PRIVATE_KEY);
    }

    @Override
    public Mono<String> getServerCertificate() {
        log.debug("Returning mock server certificate");
        return Mono.just(MOCK_CERTIFICATE);
    }

    @Override
    public Mono<String> getApiKey() {
        log.debug("Returning mock API key");
        return Mono.just(MOCK_API_KEY);
    }

    @Override
    public Mono<String> getApiSecret() {
        log.debug("Returning mock API secret");
        return Mono.just(MOCK_API_SECRET);
    }
}