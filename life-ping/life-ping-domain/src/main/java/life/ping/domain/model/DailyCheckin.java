package life.ping.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record DailyCheckin(
        UUID id,
        UUID userId,
        LocalDate localDate,
        LocalDateTime checkedInAt,
        String source
) {
}
