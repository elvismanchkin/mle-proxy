package dev.example.visa.config;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.client.DefaultHttpClientConfiguration;
import io.micronaut.http.client.HttpClientConfiguration;
import io.micronaut.http.ssl.ClientAuthentication;
import io.micronaut.http.ssl.SslConfiguration;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

@Slf4j
@Factory
public class HttpClientConfig {

    @Value("${visa.api.base-url}")
    private String baseUrl;

    @Value("${visa.api.connection-timeout:10s}")
    private Duration connectionTimeout;

    @Value("${visa.api.read-timeout:30s}")
    private Duration readTimeout;

    @Value("${visa.api.max-connections:20}")
    private int maxConnections;

    @Value("${visa.security.ssl.keystore-path}")
    private String keystorePath;

    @Value("${visa.security.ssl.keystore-password}")
    private String keystorePassword;

    @Value("${visa.security.ssl.key-password}")
    private String keyPassword;

    @Value("${visa.security.ssl.truststore-path}")
    private String truststorePath;

    @Value("${visa.security.ssl.truststore-password}")
    private String truststorePassword;

    @Bean
    @Named("visaHttpClient")
    @Singleton
    public HttpClientConfiguration httpClientConfiguration() {
        DefaultHttpClientConfiguration configuration = new DefaultHttpClientConfiguration();

        configuration.setConnectTimeout(connectionTimeout);
        configuration.setReadTimeout(readTimeout);
        configuration.setMaxContentLength(1024 * 1024);

        SslConfiguration ssl = configuration.getSslConfiguration();

        ssl.setKeyStore(new SslConfiguration.KeyStoreConfiguration());
        ssl.getKeyStore().setPath(keystorePath);
        ssl.getKeyStore().setPassword(keystorePassword);
        ssl.getKeyStore().setType("JKS");
        ssl.setTrustStore(new SslConfiguration.TrustStoreConfiguration());
        ssl.getTrustStore().setPath(truststorePath);
        ssl.getTrustStore().setPassword(truststorePassword);
        ssl.getTrustStore().setType("JKS");
        ssl.setClientAuthentication(ClientAuthentication.NEED);
        return configuration;
    }
}
