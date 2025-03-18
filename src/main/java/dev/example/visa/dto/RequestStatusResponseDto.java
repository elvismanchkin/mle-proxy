package dev.example.visa.dto;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Builder;

import java.util.List;

@Serdeable
@Introspected
@Builder
public record RequestStatusResponseDto(
        String status,
        String consumerId,
        List<StatusDetailDto> details,
        ErrorInfoDto error
) implements BaseResponseDto {
    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public ErrorInfoDto getError() {
        return error;
    }

    @Serdeable
    @Introspected
    @Builder
    public record StatusDetailDto(
            String intentType,
            String intentValue,
            String status,
            List<ErrorDetailDto> errors
    ) {
        @Serdeable
        @Introspected
        @Builder
        public record ErrorDetailDto(
                String field,
                String reason,
                String message
        ) {
        }
    }
}