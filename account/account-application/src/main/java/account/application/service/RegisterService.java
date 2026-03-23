package account.application.service;

import account.application.command.RegisterCommand;
import account.application.result.RegisterDetails;
import account.application.mapper.AccountCommandMapper;
import account.application.spi.AccountRepository;
import account.application.usecase.RegisterUseCase;
import account.application.usecase.VerificationNotificationUseCase;
import account.application.command.SendVerifyCodeCommand;
import account.domain.model.Account;
import account.domain.model.TokenType;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import shared.domain.exception.DomainConflictException;

import java.util.Map;

/**
 * Implémentation du use case d'inscription d'un compte local.
 *
 * Le service protège l'unicité de l'email, transforme la commande d'inscription en agrégat
 * `Account`, persiste le nouveau compte basic non vérifié puis déclenche l'envoi d'un code
 * de vérification email. Il retourne l'identifiant du compte créé ainsi que l'indication
 * qu'une vérification d'adresse est attendue.
 */
@ApplicationScoped
public class RegisterService implements RegisterUseCase {

    @Inject
    AccountRepository accountRepository;
    @Inject
    VerificationNotificationUseCase verificationNotificationUseCase;
    @Inject
    AccountCommandMapper accountCommandMapper;

    @Override
    public Uni<RegisterDetails> execute(RegisterCommand command) {
        return accountRepository.findByEmail(command.email())
                .flatMap(accountOpt -> {
                    if (accountOpt.isPresent()) {
                        return Uni.createFrom().failure(
                                new DomainConflictException(
                                        "ACCOUNT_EMAIL_ALREADY_EXISTS",
                                        "account.email.already_exists",
                                        Map.of("email", command.email())
                                )
                        );
                    }

                    Account account = accountCommandMapper.toAccount(command);

                    return accountRepository.save(account)
                            .flatMap(saved -> verificationNotificationUseCase.execute(
                                            new SendVerifyCodeCommand(saved.email(), TokenType.EMAIL_VERIFICATION_CODE)
                                    )
                                    .replaceWith(new RegisterDetails(saved.id(), true)));
                });
    }
}
