package dev.example.visa.model;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Builder;

@Serdeable
@Introspected
@Builder
public record Address(
        String city,
        String state,
        String country,
        String postalCode,
        String addressLine1,
        String addressLine2,
        String addressLine3) {}
