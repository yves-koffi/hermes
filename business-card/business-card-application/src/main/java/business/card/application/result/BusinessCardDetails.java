package business.card.application.result;


import com.fasterxml.jackson.databind.JsonNode;
import java.util.UUID;

public record BusinessCardDetails(
        UUID id,
        String avatarUrl,
        JsonNode raw,
        String type,
        Boolean bin,
        Long saveAt,
        String uid,
        String status
) {
}
