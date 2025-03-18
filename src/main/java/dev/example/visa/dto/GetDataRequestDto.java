package dev.example.visa.dto;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Builder;

@Serdeable
@Introspected
@Builder
public record GetDataRequestDto(
        // Intent fields
        String intentType,
        String intentValue,

        // Consumer ID
        String consumerId) {}
