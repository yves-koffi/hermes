package life.ping.application.usecase;

import io.smallrye.mutiny.Uni;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.UUID;

public interface ConnectorUseCase {
    Uni<Output> handle(@Valid @NotNull Input in);

    record Input(
            @NotBlank String appUid,
            @NotBlank String deviceUniqueId,
            @NotBlank String deviceModel,
            @NotBlank
            @Pattern(regexp = "ANDROID|IOS")
            String devicePlatform,     // ANDROID / IOS
            @NotBlank String timezone
    ) {
    }

    record Output(
            UUID accountId,
            String accessToken,
            String timezone
    ) {
    }
}
