package business.card.application.usecase;

import business.card.application.command.BusinessCardCommand;
import io.smallrye.mutiny.Uni;

import java.util.List;

public interface PushUseCase {
    Uni<Void> execute(List<BusinessCardCommand> cmd);
}
