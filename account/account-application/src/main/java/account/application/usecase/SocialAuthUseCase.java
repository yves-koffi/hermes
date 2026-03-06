package account.application.usecase;

import account.application.command.SocialCredentialCommand;
import io.smallrye.mutiny.Uni;
import shared.domain.model.TokenPair;

public interface SocialAuthUseCase {
    Uni<TokenPair> execute(SocialCredentialCommand command);
}
