package account.application.usecase;

import io.smallrye.mutiny.Uni;

public interface DeactivateAccountUseCase {
    Uni<Void> execute();
}
