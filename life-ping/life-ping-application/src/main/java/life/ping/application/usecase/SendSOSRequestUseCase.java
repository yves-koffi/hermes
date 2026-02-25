package life.ping.application.usecase;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public interface SendSOSRequestUseCase {

   Output handle(Input in);

    record Input(
            UUID userId,
            LocalDate localDate
    ) {}

    record Output(
            Instant sendAt
    ) {}
}
