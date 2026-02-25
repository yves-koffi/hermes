package business.card.application.usecase;

import business.card.application.command.BusinessCardCommand;
import business.card.application.result.BusinessCardDetails;
import io.smallrye.mutiny.Uni;

import java.util.List;

public interface SyncUseCase {
    Uni<List<BusinessCardDetails>> execute(List<BusinessCardCommand> cmd);
}
