package dev.example.visa.dto;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Builder;

import java.util.List;

@Serdeable
@Introspected
@Builder
public record ManageConsumerInformationRequestDto(
        // Intent fields
        String intentType,
        String intentValue,

        // Consumer fields
        String consumerId,
        String firstName,
        String lastName,
        String middleName,
        String countryCode,
        List<String> emails,
        List<String> phones,
        String locale,

        // Consent fields
        String consentVersion,
        String consentPresenter,
        String consentTimestamp,

        // Status
        String status
) {
}