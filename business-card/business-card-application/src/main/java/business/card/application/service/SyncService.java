package business.card.application.service;

import business.card.application.command.BusinessCardCommand;
import business.card.application.result.BusinessCardDetails;
import business.card.application.usecase.SyncUseCase;
import business.card.application.usecase.PullUseCase;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class SyncService implements SyncUseCase {

    @Inject
    PushService pushService;
    @Inject
    PullUseCase pullUseCase;


    @Override
    public Uni<List<BusinessCardDetails>> execute(List<BusinessCardCommand> commands) {
        Uni<Void> pushPhase = Uni.createFrom().voidItem();
        if (commands != null && !commands.isEmpty()) {
            pushPhase = Multi.createFrom().iterable(commands)
                    .onItem().transformToUniAndConcatenate(pushService::apply)
                    .collect().asList()
                    .replaceWithVoid();
        }

        return pushPhase.flatMap(ignored -> pullUseCase.execute());
    }
}
