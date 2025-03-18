package dev.example.visa.security;

import io.micronaut.context.annotation.Value;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.context.event.ShutdownEvent;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

@Slf4j
@Singleton
public class CertificateManager implements ApplicationEventListener<ShutdownEvent> {

    @Value("${visa.security.ssl.certificate-path}")
    private String certificatePath;

    @Value("${visa.security.ssl.keystore-path}")
    private String keystorePath;

    @Value("${visa.security.ssl.truststore-path}")
    private String truststorePath;

    @Value("${visa.security.ssl.keystore-password}")
    private String keystorePassword;

    @Value("${visa.security.ssl.truststore-password}")
    private String truststorePassword;

    @Value("${visa.security.ssl.key-password}")
    private String keyPassword;

    private final VaultService vaultService;

    public CertificateManager(VaultService vaultService) {
        this.vaultService = vaultService;
    }

    @PostConstruct
    public void initialize() {
        createCertificateDirectory();
        Mono.from(loadCertificatesFromVault())
                .doOnNext(success -> log.info("Certificates loaded successfully"))
                .doOnError(error -> log.error("Failed to load certificates", error))
                .block();
    }

    public Publisher<Boolean> loadCertificatesFromVault() {
        return vaultService.getClientCertificate()
                .zipWith(vaultService.getClientKey())
                .zipWith(vaultService.getServerCertificate())
                .flatMap(tuple -> {
                    String clientCert = tuple.getT1().getT1();
                    String clientKey = tuple.getT1().getT2();
                    String serverCert = tuple.getT2();

                    try {
                        createKeystore(clientCert, clientKey);
                        createTruststore(serverCert);
                        return Mono.just(true);
                    } catch (Exception e) {
                        return Mono.error(new RuntimeException("Failed to create keystore/truststore", e));
                    }
                });
    }

    private void createKeystore(String clientCertPem, String clientKeyPem) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(null, keystorePassword.toCharArray());

        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        Certificate certificate = cf.generateCertificate(
                new java.io.ByteArrayInputStream(clientCertPem.getBytes()));

        PrivateKey privateKey = loadPrivateKeyFromPEM(clientKeyPem);

        keyStore.setKeyEntry("client", privateKey, keyPassword.toCharArray(),
                new Certificate[]{certificate});

        try (FileOutputStream fos = new FileOutputStream(keystorePath)) {
            keyStore.store(fos, keystorePassword.toCharArray());
        }

        log.info("Keystore created at {}", keystorePath);
    }

    private void createTruststore(String serverCertPem) throws Exception {
        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(null, truststorePassword.toCharArray());

        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        Certificate certificate = cf.generateCertificate(
                new java.io.ByteArrayInputStream(serverCertPem.getBytes()));

        trustStore.setCertificateEntry("server", certificate);

        try (FileOutputStream fos = new FileOutputStream(truststorePath)) {
            trustStore.store(fos, truststorePassword.toCharArray());
        }

        log.info("Truststore created at {}", truststorePath);
    }

    private PrivateKey loadPrivateKeyFromPEM(String pemKey) throws Exception {
        String privateKeyPEM = pemKey
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] encodedKey = Base64.getDecoder().decode(privateKeyPEM);

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }

    private void createCertificateDirectory() {
        Path path = Paths.get(certificatePath);
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
                log.info("Created certificate directory: {}", certificatePath);
            } catch (IOException e) {
                log.error("Failed to create certificate directory", e);
                throw new RuntimeException("Failed to create certificate directory", e);
            }
        }
    }

    @Override
    public void onApplicationEvent(ShutdownEvent event) {
        cleanupCertificates();
    }

    private void cleanupCertificates() {
        log.info("Cleaning up certificates...");
        try {
            File keystoreFile = new File(keystorePath);
            File truststoreFile = new File(truststorePath);

            if (keystoreFile.exists() && keystoreFile.delete()) {
                log.info("Deleted keystore file: {}", keystorePath);
            }

            if (truststoreFile.exists() && truststoreFile.delete()) {
                log.info("Deleted truststore file: {}", truststorePath);
            }
        } catch (Exception e) {
            log.error("Error cleaning up certificates", e);
        }
    }
}