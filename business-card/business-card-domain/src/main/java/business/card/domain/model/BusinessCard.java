package business.card.domain.model;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;
import com.fasterxml.jackson.databind.JsonNode;

public record BusinessCard(
        UUID id,
        String uid,
        UUID accountId, // Le UUID de l'Account associé
        JsonNode raw,   // Pour JSONB. Si vous n'utilisez pas Jackson, utilisez un String ou Map
        BusinessCardType type,      // Utilisation d'une enum pour le champ 'type'
        String avatarUrl,
        OffsetDateTime softDeletedAt,
        LocalDateTime saveAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public boolean isDeleted() {
        return softDeletedAt != null;
    }
}