package business.card.application.usecase;

import io.smallrye.mutiny.Uni;
import java.util.UUID;

public interface FindImageUseCase {
    Uni<String> execute(UUID businessCardId);
}
