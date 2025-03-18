package dev.example.visa.dto;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Builder;

@Serdeable
@Introspected
@Builder
public record AddressDto(
        String city,
        String state,
        String country,
        String postalCode,
        String addressLine1,
        String addressLine2,
        String addressLine3) {}
