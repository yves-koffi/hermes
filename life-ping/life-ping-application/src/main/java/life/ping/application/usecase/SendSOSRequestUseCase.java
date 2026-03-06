package life.ping.application.usecase;

import io.smallrye.mutiny.Uni;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public interface SendSOSRequestUseCase {

    Uni<Output> handle(Input in);

    record Input(
            UUID userId,
            Instant localDate
    ) {}

    record Output(
            Instant sendAt
    ) {}
}
