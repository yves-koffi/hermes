package life.ping.domain.model;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record MissedDayMarker(
        UUID id,
        UUID accountId,
        LocalDate localDate,
        OffsetDateTime createdAt
) {
}
