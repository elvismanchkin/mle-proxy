package dev.example.visa.security;

import io.github.jopenlibs.vault.Vault;
import io.github.jopenlibs.vault.VaultConfig;
import io.github.jopenlibs.vault.VaultException;
import io.github.jopenlibs.vault.response.LogicalResponse;
import io.micronaut.context.annotation.Value;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.context.event.StartupEvent;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Singleton
public class VaultService implements ApplicationEventListener<StartupEvent> {

    @Value("${visa.security.vault.enabled}")
    private boolean vaultEnabled;

    @Value("${visa.security.vault.address}")
    private String vaultAddress;

    @Value("${visa.security.vault.app-role-id}")
    private String appRoleId;

    @Value("${visa.security.vault.app-role-secret-id}")
    private String appRoleSecretId;

    @Value("${visa.security.vault.secret-path}")
    private String secretPath;

    private Vault vault;

    @Override
    public void onApplicationEvent(StartupEvent event) {
        if (vaultEnabled) {
            initializeVault();
        } else {
            log.info("Vault integration is disabled");
        }
    }

    private void initializeVault() {
        try {
            log.info("Initializing Vault client with address: {}", vaultAddress);

            final VaultConfig config = new VaultConfig()
                    .address(vaultAddress)
                    .build();

            vault = Vault.create(config);

            String token = vault.auth().loginByAppRole(appRoleId, appRoleSecretId).getAuthClientToken();
            log.info("Successfully authenticated with Vault using AppRole");

            config.token(token);

        } catch (VaultException e) {
            log.error("Failed to initialize Vault", e);
            throw new RuntimeException("Failed to initialize Vault", e);
        }
    }

    public Mono<String> getClientCertificate() {
        return getSecret("client_certificate");
    }

    public Mono<String> getClientKey() {
        return getSecret("client_key");
    }

    public Mono<String> getServerCertificate() {
        return getSecret("server_certificate");
    }

    public Mono<String> getApiKey() {
        return getSecret("api_key");
    }

    public Mono<String> getApiSecret() {
        return getSecret("api_secret");
    }

    private Mono<String> getSecret(String key) {
        if (!vaultEnabled) {
            log.warn("Vault is disabled, cannot retrieve secret: {}", key);
            return Mono.error(new IllegalStateException("Vault is disabled"));
        }

        return Mono.fromCallable(() -> {
            try {
                LogicalResponse response = vault.logical().read(secretPath);
                Map<String, String> data = response.getData();

                if (!data.containsKey(key)) {
                    throw new RuntimeException("Secret not found: " + key);
                }

                return data.get(key);
            } catch (VaultException e) {
                log.error("Failed to retrieve secret: {}", key, e);
                throw new RuntimeException("Failed to retrieve secret: " + key, e);
            }
        });
    }
}