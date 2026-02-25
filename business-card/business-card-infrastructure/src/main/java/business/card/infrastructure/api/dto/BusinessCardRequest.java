package business.card.infrastructure.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BusinessCardRequest(
        Integer id,
        String raw,
        String type,
        Integer bin,
        @JsonProperty("save_at") Long saveAt,
        String uid,
        String status
) {
}
