package account.application.service;

import account.application.command.ForgetPasswordCommand;
import account.application.event.EmailDispatchEvent;
import account.application.result.ForgetPasswordResult;
import account.application.spi.AccountRepository;
import account.application.spi.HashTokenRepository;
import account.application.spi.NotificationByEmailDispatchPublisher;
import account.application.usecase.ForgetPasswordUseCase;
import account.domain.model.HashToken;
import account.domain.model.Provider;
import account.domain.model.TokenType;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import shared.application.context.RequestContext;
import shared.domain.exception.DomainConflictException;

import java.util.Locale;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Implémentation du use case de demande de réinitialisation de mot de passe.
 *
 * Le service valide l'email, vérifie l'existence du compte puis émet un token ou un code
 * de reset dédié. Les anciens tokens de reset encore actifs pour ce compte sont supprimés
 * avant la création du nouveau afin de ne conserver qu'un secret valide à la fois. Enfin,
 * il envoie l'email correspondant en réutilisant les templates applicatifs.
 */
@ApplicationScoped
public class ForgetPasswordService implements ForgetPasswordUseCase {

    private static final Logger LOGGER = Logger.getLogger(ForgetPasswordService.class);

    @Inject
    AccountRepository accountRepository;
    @Inject
    HashTokenRepository hashTokenRepository;
    @Inject
    RequestContext context;
    @Inject
    OneTimeTokenService oneTimeTokenService;
    @Inject
    AccountMetrics accountMetrics;
    @Inject
    NotificationByEmailDispatchPublisher emailDispatchPublisher;

    @Override
    public Uni<ForgetPasswordResult> execute(ForgetPasswordCommand command) {
        if (command.email() == null || command.email().isBlank()) {
            return Uni.createFrom().failure(
                    new DomainConflictException(
                            "INVALID_EMAIL",
                            "account.email.invalid",
                            Map.of()
                    )
            );
        }

        TokenType tokenType = command.type() == null ? TokenType.PASSWORD_RESET_CODE : command.type();
        if (tokenType != TokenType.PASSWORD_RESET_CODE && tokenType != TokenType.PASSWORD_RESET_LINK) {
            return Uni.createFrom().failure(
                    new DomainConflictException(
                            "INVALID_RESET_TOKEN_TYPE",
                            "account.reset_password.token.invalid_type",
                            Map.of("type", tokenType.name())
                    )
            );
        }

        String normalizedEmail = normalizeEmail(command.email());
        return accountRepository.findByEmail(normalizedEmail)
                .flatMap(accountOpt -> {
                    if (accountOpt.isEmpty() || accountOpt.get().provider() != Provider.BASIC) {
                        return Uni.createFrom().item(new ForgetPasswordResult(
                                true,
                                "EMAIL",
                                tokenType,
                                null
                        ));
                    }

                    String tokenValue = oneTimeTokenService.generateToken(tokenType);
                    HashToken hashToken = new HashToken(
                            UUID.randomUUID(),
                            oneTimeTokenService.hash(tokenValue),
                            tokenType,
                            accountOpt.get().id(),
                            oneTimeTokenService.computeExpiryDate(tokenType),
                            context.getExecutionContext() != null ? context.getExecutionContext().ip() : null,
                            null,
                            null,
                            null
                    );

                    return hashTokenRepository.deleteByAccountIdAndTokenTypes(
                                    accountOpt.get().id(),
                                    List.of(TokenType.PASSWORD_RESET_CODE, TokenType.PASSWORD_RESET_LINK)
                            )
                            .flatMap(ignored -> hashTokenRepository.save(hashToken))
                            .flatMap(saved -> emailDispatchPublisher.publish(new EmailDispatchEvent(
                                    accountOpt.get().email(),
                                    tokenType,
                                    resolveLanguage(),
                                    tokenValue
                            )))
                            .invoke(() -> {
                                accountMetrics.recordPasswordResetRequested();
                                LOGGER.infov(
                                        "event=password_reset_requested accountId={0} email={1} type={2}",
                                        accountOpt.get().id(),
                                        accountOpt.get().email(),
                                        tokenType
                                );
                            })
                            .replaceWith(new ForgetPasswordResult(
                                    true,
                                    "EMAIL",
                                    tokenType,
                                    hashToken.expiryDate()
                            ));
                });
    }

    private String resolveLanguage() {
        if (context.getExecutionContext() == null || context.getExecutionContext().language() == null || context.getExecutionContext().language().isBlank()) {
            return "fr";
        }
        return context.getExecutionContext().language();
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
