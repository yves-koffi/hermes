package business.card.domain.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;

public record BusinessCardPatch(
        JsonNode raw,
        BusinessCardType type,
        LocalDateTime saveAt
) {
}
