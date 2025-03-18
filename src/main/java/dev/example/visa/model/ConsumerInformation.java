package dev.example.visa.model;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Builder;

import java.util.List;

@Serdeable
@Introspected
@Builder
public record ConsumerInformation(
        List<String> emails,
        String locale,
        List<String> phones,
        Consent consent,
        String lastName,
        String firstName,
        String middleName,
        String countryCode,
        String externalConsumerID,
        List<NationalIdentifier> nationalIdentifiers
) {
}
