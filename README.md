# Visa Click to Pay Integration Service

A reactive Micronaut service for integrating with Visa Click to Pay APIs. This service acts as a client to the Visa ID
and Credential API, exposing functionality through RabbitMQ RPC patterns.

## Technology Stack

- Java 21
- Micronaut Framework
- Project Reactor for reactive programming
- RabbitMQ for async communication using RPC pattern
- Maven as the build tool
- HashiCorp Vault for secure credential management
- Mutual TLS for Visa API communication

## Architecture

This service follows a stateless architecture:

- No database persistence
- Acts as a client to Visa APIs
- Exposes functionality via RabbitMQ RPC pattern
- Uses Java records for immutable data models
- Implements reactive programming patterns with Project Reactor

## Features

- Support for all Visa Click to Pay API operations
- Secure credential management with HashiCorp Vault
- Mutual TLS for secure communication with Visa APIs
- Reactive programming model
- Comprehensive logging with sensitive data masking
- Health check endpoints for monitoring
- Robust error handling

## Configuration

### application.yml

The service can be configured for both sandbox and production environments:

```yaml
visa:
  api:
    environment: sandbox  # sandbox or production
    sandbox:
      base-url: https://sandbox.api.visa.com
      key-store-path: ${VISA_SANDBOX_KEYSTORE_PATH}
      key-store-password: ${VISA_SANDBOX_KEYSTORE_PASSWORD}
      key-password: ${VISA_SANDBOX_KEY_PASSWORD}
      user-id: ${VISA_SANDBOX_USER_ID}
      password: ${VISA_SANDBOX_PASSWORD}
    production:
      base-url: https://api.visa.com
      key-store-path: ${VISA_PRODUCTION_KEYSTORE_PATH}
      key-store-password: ${VISA_PRODUCTION_KEYSTORE_PASSWORD}
      key-password: ${VISA_PRODUCTION_KEY_PASSWORD}
      user-id: ${VISA_PRODUCTION_USER_ID}
      password: ${VISA_PRODUCTION_PASSWORD}
```

### Vault Integration

HashiCorp Vault is used for secure credential management:

```yaml
vault:
  enabled: true
  address: ${VAULT_ADDR:http://localhost:8200}
  token: ${VAULT_TOKEN}
  secret-path: secret/visa-click-to-pay
  app-role:
    role-id: ${VAULT_ROLE_ID}
    secret-id: ${VAULT_SECRET_ID}
```

### RabbitMQ Configuration

```yaml
rabbitmq:
  uri: ${RABBITMQ_URI:amqp://localhost:5672}
  username: ${RABBITMQ_USERNAME:guest}
  password: ${RABBITMQ_PASSWORD:guest}
  rpc:
    request-queue: visa-click-to-pay-requests
    timeout: 30s
```

## API Operations

The service supports all Visa Click to Pay API operations:

1. `enrollData` - Enroll consumer information and payment instrument
2. `enrollPaymentInstruments` - Enroll payment instruments for existing consumer
3. `requestStatus` - Check status of submitted requests
4. `managePaymentInstruments` - Update payment instruments
5. `manageConsumerInformation` - Update consumer information
6. `deleteConsumerInformation` - Delete consumer information
7. `deletePaymentInstruments` - Delete payment instruments
8. `getData` - Retrieve consumer and payment data

## Building and Running

### Prerequisites

- Java 21 JDK
- Maven 3.8+
- RabbitMQ server
- HashiCorp Vault (optional, can be disabled)

### Build

```bash
mvn clean package
```

### Run

```bash
java -jar target/visa-click-to-pay-1.0.0-SNAPSHOT.jar
```

### Environment Variables

The following environment variables should be set:

```bash
# Visa API Credentials
export VISA_SANDBOX_KEYSTORE_PATH=/path/to/keystore.jks
export VISA_SANDBOX_KEYSTORE_PASSWORD=keystore-password
export VISA_SANDBOX_KEY_PASSWORD=key-password
export VISA_SANDBOX_USER_ID=user-id
export VISA_SANDBOX_PASSWORD=password

# Vault Configuration
export VAULT_ADDR=http://localhost:8200
export VAULT_TOKEN=vault-token
# Or use AppRole authentication
export VAULT_ROLE_ID=role-id
export VAULT_SECRET_ID=secret-id

# RabbitMQ Configuration
export RABBITMQ_URI=amqp://localhost:5672
export RABBITMQ_USERNAME=guest
export RABBITMQ_PASSWORD=guest
```

## Health Check

The service provides a health check endpoint at:

```
GET /health
```

## Security

- All API credentials are stored securely in HashiCorp Vault
- Communication with Visa API uses mutual TLS (mTLS)
- Sensitive data is masked in logs
- All communication is over HTTPS

## Message Handling

The service uses RabbitMQ RPC pattern for handling requests. Each API operation has a dedicated queue:

- `visa-click-to-pay.enrollData`
- `visa-click-to-pay.enrollPaymentInstruments`
- `visa-click-to-pay.requestStatus`
- `visa-click-to-pay.managePaymentInstruments`
- `visa-click-to-pay.manageConsumerInformation`
- `visa-click-to-pay.deleteConsumerInformation`
- `visa-click-to-pay.deletePaymentInstruments`
- `visa-click-to-pay.getData`

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Micronaut 4.7.6 Documentation

- [User Guide](https://docs.micronaut.io/4.7.6/guide/index.html)
- [API Reference](https://docs.micronaut.io/4.7.6/api/index.html)
- [Configuration Reference](https://docs.micronaut.io/4.7.6/guide/configurationreference.html)
- [Micronaut Guides](https://guides.micronaut.io/index.html)

---

- [Rewrite Micronaut3to4Migration Recipe](https://docs.openrewrite.org/running-recipes/popular-recipe-guides/migrate-to-micronaut-4-from-micronaut-3)
- [Micronaut Maven Plugin documentation](https://micronaut-projects.github.io/micronaut-maven-plugin/latest/)

## Feature tracing-opentelemetry-exporter-otlp documentation

- [Micronaut OpenTelemetry Exporter OTLP documentation](http://localhost/micronaut-tracing/guide/index.html#opentelemetry)

- [https://opentelemetry.io](https://opentelemetry.io)

## Feature tracing-opentelemetry-annotations documentation

- [Micronaut OpenTelemetry Annotations documentation](https://micronaut-projects.github.io/micronaut-tracing/latest/guide/#opentelemetry)

- [https://opentelemetry.io](https://opentelemetry.io)

## Feature micronaut-aot documentation

- [Micronaut AOT documentation](https://micronaut-projects.github.io/micronaut-aot/latest/guide/)

## Feature http-client documentation

- [Micronaut HTTP Client documentation](https://docs.micronaut.io/latest/guide/index.html#nettyHttpClient)

## Feature problem-json documentation

- [Micronaut Problem JSON documentation](https://micronaut-projects.github.io/micronaut-problem-json/latest/guide/index.html)

## Feature rabbitmq documentation

- [Micronaut RabbitMQ Messaging documentation](https://micronaut-projects.github.io/micronaut-rabbitmq/latest/guide/index.html)

## Feature mockito documentation

- [https://site.mockito.org](https://site.mockito.org)

## Feature tracing-opentelemetry-exporter-jaeger documentation

- [Micronaut OpenTelemetry Exporter Jaeger documentation](http://localhost/micronaut-tracing/guide/index.html#opentelemetry)

- [https://opentelemetry.io](https://opentelemetry.io)

## Feature lombok documentation

- [Micronaut Project Lombok documentation](https://docs.micronaut.io/latest/guide/index.html#lombok)

- [https://projectlombok.org/features/all](https://projectlombok.org/features/all)

## Feature tracing-opentelemetry-exporter-logging documentation

- [Micronaut OpenTelemetry Exporter Logging documentation](http://localhost/micronaut-tracing/guide/index.html#opentelemetry)

- [https://opentelemetry.io](https://opentelemetry.io)

## Feature validation documentation

- [Micronaut Validation documentation](https://micronaut-projects.github.io/micronaut-validation/latest/guide/)

## Feature test-resources documentation

- [Micronaut Test Resources documentation](https://micronaut-projects.github.io/micronaut-test-resources/latest/guide/)

## Feature reactor documentation

- [Micronaut Reactor documentation](https://micronaut-projects.github.io/micronaut-reactor/snapshot/guide/index.html)

## Feature retry documentation

- [Micronaut Retry documentation](https://docs.micronaut.io/latest/guide/#retry)

## Feature tracing-opentelemetry-jaeger documentation

- [Micronaut OpenTelemetry Jaeger documentation](https://micronaut-projects.github.io/micronaut-tracing/latest/guide/#opentelemetry)

- [https://opentelemetry.io](https://opentelemetry.io)

## Feature tracing-opentelemetry-http documentation

- [Micronaut OpenTelemetry HTTP documentation](http://localhost/micronaut-tracing/guide/index.html#opentelemetry)

## Feature control-panel documentation

- [Micronaut Control Panel documentation](https://micronaut-projects.github.io/micronaut-control-panel/latest/guide/index.html)

## Feature management documentation

- [Micronaut Management documentation](https://docs.micronaut.io/latest/guide/index.html#management)

## Feature maven-enforcer-plugin documentation

- [https://maven.apache.org/enforcer/maven-enforcer-plugin/](https://maven.apache.org/enforcer/maven-enforcer-plugin/)

## Feature junit-params documentation

- [https://junit.org/junit5/docs/current/user-guide/#writing-tests-parameterized-tests](https://junit.org/junit5/docs/current/user-guide/#writing-tests-parameterized-tests)

## Feature annotation-api documentation

- [https://jakarta.ee/specifications/annotations/](https://jakarta.ee/specifications/annotations/)

## Feature openrewrite documentation

- [https://docs.openrewrite.org/](https://docs.openrewrite.org/)


