package dev.example.visa.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.time.Duration;

/**
 * TestContainer setup for HashiCorp Vault.
 * This is provided as an example but not used in the main integration tests since we're mocking
 * the Visa API client directly.
 */
public class VaultTestContainer extends GenericContainer<VaultTestContainer> {
    private static final Logger LOG = LoggerFactory.getLogger(VaultTestContainer.class);

    private static final String VAULT_IMAGE = "hashicorp/vault:1.15";
    private static final int VAULT_PORT = 8200;

    private String rootToken = "dev-only-token";

    public VaultTestContainer() {
        super(VAULT_IMAGE);

        this.withExposedPorts(VAULT_PORT)
                .withEnv("VAULT_DEV_ROOT_TOKEN_ID", rootToken)
                .withEnv("VAULT_DEV_LISTEN_ADDRESS", "0.0.0.0:8200")
                .withEnv("VAULT_ADDR", "http://0.0.0.0:8200")
                .withLogConsumer(new Slf4jLogConsumer(LOG))
                .waitingFor(Wait.forHttp("/v1/sys/health")
                        .forPort(VAULT_PORT)
                        .withStartupTimeout(Duration.ofSeconds(30)));
    }

    /**
     * Returns the URL for connecting to Vault.
     */
    public String getVaultUri() {
        return String.format("http://%s:%d", getHost(), getMappedPort(VAULT_PORT));
    }

    /**
     * Returns the root token for initial Vault operations.
     */
    public String getRootToken() {
        return rootToken;
    }

    /**
     * Example method to configure Vault for testing.
     * This would set up the necessary secrets and policies.
     */
    public void configureVault() {
        // This would be implemented to set up test data in Vault
        // For example:
        //  - Enable KV secrets engine
        //  - Create policies
        //  - Add test secrets
        //  - Configure AppRole auth method

        LOG.info("Vault is configured and ready at: {}", getVaultUri());
    }
}