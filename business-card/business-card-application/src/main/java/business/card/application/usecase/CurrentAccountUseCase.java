package business.card.application.usecase;

import business.card.application.result.AccountDetails;
import io.smallrye.mutiny.Uni;

public interface CurrentAccountUseCase {
    Uni<AccountDetails> execute();
}
