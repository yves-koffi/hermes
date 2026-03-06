package account.application.usecase;

import io.smallrye.mutiny.Uni;

public interface SendVerifyCodeUseCase {
    Uni<Void> execute();
}
