package account.application.usecase;

import account.application.result.AccountDetails;
import io.smallrye.mutiny.Uni;

public interface CurrentAccountUseCase {
    Uni<AccountDetails> execute();
}
