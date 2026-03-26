package account.application.service;

import account.application.command.VerifyAccountCommand;
import account.application.result.AccountVerificationResult;
import account.application.spi.AccountRepository;
import account.application.spi.HashTokenRepository;
import account.application.usecase.CheckVerifyEmailUseCase;
import account.domain.model.Account;
import account.domain.model.AccountSecurityEventType;
import account.domain.model.HashToken;
import account.domain.model.TokenType;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import shared.domain.exception.DomainConflictException;
import shared.domain.exception.DomainNotFoundException;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Implémentation du use case de validation d'adresse email.
 *
 * Le service reçoit un token brut, le hash avant recherche, vérifie qu'il correspond bien
 * à un token de vérification encore actif puis recharge le compte ciblé. Si le compte n'est
 * pas encore activé, il renseigne la date d'activation et persiste la nouvelle version.
 * Le token consommé est ensuite supprimé pour garantir un usage unique.
 */
@ApplicationScoped
public class CheckVerifyEmailService implements CheckVerifyEmailUseCase {

    private static final Logger LOGGER = Logger.getLogger(CheckVerifyEmailService.class);

    @Inject
    HashTokenRepository hashTokenRepository;
    @Inject
    AccountRepository accountRepository;
    @Inject
    OneTimeTokenService oneTimeTokenService;
    @Inject
    AccountMetrics accountMetrics;
    @Inject
    AccountSecurityEventService accountSecurityEventService;

    @Override
    public Uni<AccountVerificationResult> execute(VerifyAccountCommand command) {
        if (command.token() == null || command.token().isBlank()) {
            return Uni.createFrom().failure(
                    new DomainConflictException(
                            "INVALID_VERIFY_TOKEN",
                            "account.verify_token.invalid",
                            Map.of()
                    )
            );
        }

        String hashToken = oneTimeTokenService.hash(command.token());
        return hashTokenRepository.findByHashToken(hashToken)
                .flatMap(tokenOpt -> {
                    if (tokenOpt.isEmpty()) {
                        return Uni.createFrom().failure(
                                new DomainNotFoundException(
                                        "VERIFY_TOKEN_NOT_FOUND",
                                        "account.verify_token.not_found",
                                        Map.of("token", command.token())
                                )
                        );
                    }

                    HashToken token = tokenOpt.get();
                    if (token.tokenType() != TokenType.EMAIL_VERIFICATION_LINK
                            && token.tokenType() != TokenType.EMAIL_VERIFICATION_CODE) {
                        return Uni.createFrom().failure(
                                new DomainConflictException(
                                        "INVALID_VERIFY_TOKEN_TYPE",
                                        "account.verify_token.invalid_type",
                                        Map.of("type", token.tokenType().name())
                                )
                        );
                    }

                    if (token.revokedAt() != null || token.expiryDate().isBefore(OffsetDateTime.now())) {
                        return Uni.createFrom().failure(
                                new DomainConflictException(
                                        "VERIFY_TOKEN_EXPIRED",
                                        "account.verify_token.expired",
                                        Map.of("expiryDate", token.expiryDate())
                                )
                        );
                    }
                    if (command.type() != null && token.tokenType() != command.type()) {
                        return Uni.createFrom().failure(
                                new DomainConflictException(
                                        "INVALID_VERIFY_TOKEN_TYPE",
                                        "account.verify_token.invalid_type",
                                        Map.of("type", token.tokenType().name())
                                )
                        );
                    }

                    return accountRepository.findById(token.accountId())
                            .flatMap(accountOpt -> {
                                if (accountOpt.isEmpty()) {
                                    return Uni.createFrom().failure(
                                            new DomainNotFoundException(
                                                    "ACCOUNT_NOT_FOUND",
                                                    "account.not_found",
                                                    Map.of("id", token.accountId())
                                            )
                                    );
                                }

                                Account current = accountOpt.get();
                                if (current.isActivated()) {
                                    return hashTokenRepository.deleteById(token.id())
                                            .invoke(() -> {
                                                accountMetrics.recordEmailVerificationSucceeded();
                                                LOGGER.infov(
                                                        "event=account_email_verified accountId={0} email={1} verifiedAt={2}",
                                                        current.id(),
                                                        current.email(),
                                                        current.activatedAt()
                                                );
                                            })
                                            .replaceWith(new AccountVerificationResult(
                                                    current.id(),
                                                    true,
                                                    current.activatedAt()
                                            ));
                                }
                                OffsetDateTime verifiedAt = current.activatedAt() == null
                                        ? OffsetDateTime.now()
                                        : current.activatedAt();

                                return accountRepository.save(current.activate(verifiedAt, OffsetDateTime.now()))
                                        .flatMap(saved -> hashTokenRepository.deleteById(token.id())
                                                .flatMap(ignored -> accountSecurityEventService.record(
                                                        saved,
                                                        AccountSecurityEventType.EMAIL_VERIFIED,
                                                        "Email verified"
                                                ))
                                                .invoke(() -> {
                                                    accountMetrics.recordEmailVerificationSucceeded();
                                                    LOGGER.infov(
                                                            "event=account_email_verified accountId={0} email={1} verifiedAt={2}",
                                                            saved.id(),
                                                            saved.email(),
                                                            saved.activatedAt()
                                                    );
                                                })
                                                .replaceWith(new AccountVerificationResult(
                                                        saved.id(),
                                                        true,
                                                        saved.activatedAt()
                                                )));
                            });
                });
    }
}
