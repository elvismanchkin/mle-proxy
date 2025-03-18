package dev.example.visa.dto;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
@Introspected
public interface BaseResponseDto {
    String getStatus();

    ErrorInfoDto getError();
}