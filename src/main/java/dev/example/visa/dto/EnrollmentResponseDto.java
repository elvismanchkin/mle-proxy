package dev.example.visa.dto;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Builder;

@Serdeable
@Introspected
@Builder
public record EnrollmentResponseDto(String status, String requestId, ErrorInfoDto error) implements BaseResponseDto {
    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public ErrorInfoDto getError() {
        return error;
    }
}