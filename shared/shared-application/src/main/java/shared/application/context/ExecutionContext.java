package shared.application.context;

import io.smallrye.mutiny.Uni;

import java.util.UUID;

public interface ExecutionContext {
    Uni<UUID> getCurrentAccountId();
}
