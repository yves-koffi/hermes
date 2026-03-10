package business.card.application.usecase;

import io.smallrye.mutiny.Uni;
import java.util.UUID;

public interface FindImageByIdUseCase {
    Uni<String> execute(UUID businessCardId);
}
