package business.card.infrastructure.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record  BusinessCardResponse(
        @JsonProperty("_image") String avatarUrl,
        @JsonProperty("_raw") String raw,
        @JsonProperty("_type") String type,
        @JsonProperty("_bin") Integer bin,
        @JsonProperty("save_at") Long saveAt,
        @JsonProperty("_uid") String uid,
        @JsonProperty("_status") String status
) {
}
