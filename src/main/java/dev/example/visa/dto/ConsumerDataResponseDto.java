package dev.example.visa.dto;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Builder;

import java.util.List;

@Serdeable
@Introspected
@Builder
public record ConsumerDataResponseDto(
        String status,
        String consumerId,
        ConsumerInfoDto consumerInfo,
        List<PaymentInstrumentInfoDto> paymentInstruments,
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
    public record ConsumerInfoDto(
            String firstName,
            String lastName,
            String middleName,
            String countryCode,
            List<String> emails,
            List<String> phones,
            String locale
    ) {
    }

    @Serdeable
    @Introspected
    @Builder
    public record PaymentInstrumentInfoDto(
            String type,
            String status,
            String accountNumber,
            String accountName,
            String expirationDate,
            // Card specific
            String cardType,
            String issuerName,
            String nameOnCard,
            // Bank account specific
            String bankName,
            String accountType,
            String currencyCode
    ) {
    }
}