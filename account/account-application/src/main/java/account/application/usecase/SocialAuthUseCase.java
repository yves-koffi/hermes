package account.application.usecase;

import account.application.command.SocialCredentialCommand;
import account.application.result.AuthDetails;
import io.smallrye.mutiny.Uni;

public interface SocialAuthUseCase {
    Uni<AuthDetails> execute(SocialCredentialCommand command);
}
