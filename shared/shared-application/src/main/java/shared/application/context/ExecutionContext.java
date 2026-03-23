package shared.application.context;

import java.util.UUID;

public record ExecutionContext(
        UUID accountId,
        String ip,
        String language
) {
}
