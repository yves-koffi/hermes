package account.application.usecase;

import io.smallrye.mutiny.Uni;

public interface DeleteAccountUseCase {
    Uni<Void> execute();
}
