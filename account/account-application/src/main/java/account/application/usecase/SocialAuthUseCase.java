package account.application.usecase;

import account.application.command.SocialCredentialCommand;
import account.application.result.AuthResult;
import io.smallrye.mutiny.Uni;

public interface SocialAuthUseCase {
    Uni<AuthResult> execute(SocialCredentialCommand command);
}
