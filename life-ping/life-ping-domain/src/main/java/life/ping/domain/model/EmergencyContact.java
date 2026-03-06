package life.ping.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record EmergencyContact(
        UUID id,
        UUID accountId,
        String name,
        String email,
        String language,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
