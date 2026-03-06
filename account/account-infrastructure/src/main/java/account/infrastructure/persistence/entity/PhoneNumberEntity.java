package account.infrastructure.persistence.entity;

import jakarta.persistence.Embeddable;

@Embeddable
public record PhoneNumberEntity(
        String prefix,
        String number
) {}
