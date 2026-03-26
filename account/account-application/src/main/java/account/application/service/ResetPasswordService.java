package account.application.service;

import account.application.command.ResetPasswordCommand;
import account.application.result.PasswordResetResult;
import account.application.spi.AccountRepository;
import account.application.spi.AuthSessionRepository;
import account.application.spi.HashTokenRepository;
import account.application.usecase.ResetPasswordUseCase;
import account.domain.model.Account;
import account.domain.model.AccountSecurityEventType;
import account.domain.model.HashToken;
import account.domain.model.Provider;
import account.domain.model.TokenType;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import shared.domain.exception.DomainConflictException;
import shared.domain.exception.DomainNotFoundException;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Implémentation du use case de réinitialisation effective du mot de passe.
 *
 * Le service valide le token de reset, vérifie le type attendu, contrôle l'expiration
 * puis recharge le compte concerné. Après mise à jour du mot de passe, il révoque toutes
 * les sessions actives du compte et supprime le token de reset consommé pour empêcher
 * toute réutilisation.
 */
@ApplicationScoped
public class ResetPasswordService implements ResetPasswordUseCase {

    @Inject
    HashTokenRepository hashTokenRepository;
    @Inject
    AccountRepository accountRepository;
    @Inject
    AuthSessionRepository authSessionRepository;
    @Inject
    OneTimeTokenService oneTimeTokenService;
    @Inject
    AccountSecurityEventService accountSecurityEventService;

    @Override
    public Uni<PasswordResetResult> execute(ResetPasswordCommand command) {
        if (command.token() == null || command.token().isBlank()) {
            return Uni.createFrom().failure(
                    new DomainConflictException(
                            "INVALID_RESET_TOKEN",
                            "account.reset_password.token.invalid",
                            Map.of()
                    )
            );
        }
        if (command.newPassword() == null || command.newPassword().isBlank()) {
            return Uni.createFrom().failure(
                    new DomainConflictException(
                            "INVALID_NEW_PASSWORD",
                            "account.reset_password.new_password.invalid",
                            Map.of()
                    )
            );
        }
        if (!command.newPassword().equals(command.confirmNewPassword())) {
            return Uni.createFrom().failure(
                    new DomainConflictException(
                            "PASSWORD_CONFIRMATION_MISMATCH",
                            "account.reset_password.password_confirmation.mismatch",
                            Map.of()
                    )
            );
        }

        return hashTokenRepository.findByHashToken(oneTimeTokenService.hash(command.token()))
                .flatMap(tokenOpt -> {
                    if (tokenOpt.isEmpty()) {
                        return Uni.createFrom().failure(
                                new DomainNotFoundException(
                                        "RESET_TOKEN_NOT_FOUND",
                                        "account.reset_password.token.not_found",
                                        Map.of("token", command.token())
                                )
                        );
                    }

                    HashToken token = tokenOpt.get();
                    if (token.tokenType() != TokenType.PASSWORD_RESET_CODE
                            && token.tokenType() != TokenType.PASSWORD_RESET_LINK) {
                        return Uni.createFrom().failure(
                                new DomainConflictException(
                                        "INVALID_RESET_TOKEN_TYPE",
                                        "account.reset_password.token.invalid_type",
                                        Map.of("type", token.tokenType().name())
                                )
                        );
                    }
                    if (token.revokedAt() != null || token.expiryDate().isBefore(OffsetDateTime.now())) {
                        return Uni.createFrom().failure(
                                new DomainConflictException(
                                        "RESET_TOKEN_EXPIRED",
                                        "account.reset_password.token.expired",
                                        Map.of("expiryDate", token.expiryDate())
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

                                Account account = accountOpt.get();
                                if (account.provider() != Provider.BASIC) {
                                    return Uni.createFrom().failure(
                                            new DomainConflictException(
                                                    "ACCOUNT_PROVIDER_NOT_SUPPORTED",
                                                    "account.reset_password.provider.not_supported",
                                                    Map.of("provider", account.provider().name())
                                            )
                                    );
                                }

                                OffsetDateTime now = OffsetDateTime.now();
                                return accountRepository.save(account.withPasswordHash(BcryptUtil.bcryptHash(command.newPassword()), now))
                                        .flatMap(saved -> authSessionRepository.revokeAllByAccountId(saved.id(), now)
                                                .flatMap(ignored -> hashTokenRepository.deleteById(token.id()))
                                                .flatMap(ignored -> accountSecurityEventService.record(
                                                        saved,
                                                        AccountSecurityEventType.PASSWORD_CHANGED,
                                                        "Password reset and active sessions revoked"
                                                ))
                                                .replaceWith(new PasswordResetResult(
                                                        saved.id(),
                                                        true,
                                                        true,
                                                        now
                                                )));
                            });
                });
    }
}
