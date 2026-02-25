package business.card.application.command;

import business.card.domain.model.BusinessCardStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

public record BusinessCardCommand(
        Integer id,
        String raw,
        String type,
        Integer bin,
        @JsonProperty("save_at") Long saveAt,
        String uid,
        BusinessCardStatus status
) {
}
