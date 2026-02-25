package business.card.application.spi;

import io.smallrye.mutiny.Uni;

import java.util.UUID;

public interface CurrentAccountProvider {
    Uni<UUID> getCurrentAccountId();
}
