package account.application.service;

import account.application.command.RegisterCommand;
import account.application.result.RegisterResult;
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
import org.jboss.logging.Logger;
import shared.domain.exception.DomainConflictException;

import java.util.Locale;
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

    private static final Logger LOGGER = Logger.getLogger(RegisterService.class);

    @Inject
    AccountRepository accountRepository;
    @Inject
    VerificationNotificationUseCase verificationNotificationUseCase;
    @Inject
    AccountCommandMapper accountCommandMapper;

    @Override
    public Uni<RegisterResult> execute(RegisterCommand command) {
        if (command.name() == null || command.name().isBlank()) {
            return Uni.createFrom().failure(
                    new DomainConflictException(
                            "INVALID_ACCOUNT_NAME",
                            "account.name.invalid",
                            Map.of()
                    )
            );
        }
        if (command.password() == null || command.password().isBlank()) {
            return Uni.createFrom().failure(
                    new DomainConflictException(
                            "INVALID_ACCOUNT_PASSWORD",
                            "account.password.invalid",
                            Map.of()
                    )
            );
        }

        String normalizedEmail = normalizeEmail(command.email());
        return accountRepository.findByEmail(normalizedEmail)
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
                            .flatMap(saved -> {
                                if (!command.requiredVerifyEmail()) {
                                    return Uni.createFrom().item(new RegisterResult(
                                            saved.id(),
                                            saved.email(),
                                            false,
                                            null
                                    ))
                                            .invoke(details -> LOGGER.infov(
                                                    "event=account_registered accountId={0} email={1} verificationRequired={2}",
                                                    saved.id(),
                                                    saved.email(),
                                                    false
                                            ));
                                }

                                return verificationNotificationUseCase.execute(
                                                new SendVerifyCodeCommand(saved.email(), TokenType.EMAIL_VERIFICATION_CODE)
                                        )
                                        .replaceWith(new RegisterResult(
                                                saved.id(),
                                                saved.email(),
                                                true,
                                                "VERIFY_EMAIL"
                                        ))
                                        .invoke(details -> LOGGER.infov(
                                                "event=account_registered accountId={0} email={1} verificationRequired={2}",
                                                saved.id(),
                                                saved.email(),
                                                true
                                        ));
                            });
                });
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
