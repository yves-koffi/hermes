package account.application.service;

import account.application.command.ResetPasswordCommand;
import account.application.result.PasswordResetDetails;
import account.application.spi.AccountRepository;
import account.application.spi.HashTokenRepository;
import account.application.usecase.ResetPasswordUseCase;
import account.domain.model.Account;
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

@ApplicationScoped
public class ResetPasswordService implements ResetPasswordUseCase {

    @Inject
    HashTokenRepository hashTokenRepository;
    @Inject
    AccountRepository accountRepository;

    @Override
    public Uni<PasswordResetDetails> execute(ResetPasswordCommand command) {
        if (command.hashToken() == null || command.hashToken().isBlank()) {
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

        return hashTokenRepository.findByHashToken(command.hashToken())
                .flatMap(tokenOpt -> {
                    if (tokenOpt.isEmpty()) {
                        return Uni.createFrom().failure(
                                new DomainNotFoundException(
                                        "RESET_TOKEN_NOT_FOUND",
                                        "account.reset_password.token.not_found",
                                        Map.of("hashToken", command.hashToken())
                                )
                        );
                    }

                    HashToken token = tokenOpt.get();
                    if (token.tokenType() != TokenType.VERIFY_CODE && token.tokenType() != TokenType.VERIFY_TOKEN) {
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
                                Account updatedAccount = new Account(
                                        account.id(),
                                        account.name(),
                                        account.email(),
                                        account.phoneNumber(),
                                        BcryptUtil.bcryptHash(command.newPassword()),
                                        account.avatarUrl(),
                                        account.providerId(),
                                        account.provider(),
                                        account.activatedAt(),
                                        account.createdAt(),
                                        now
                                );

                                return accountRepository.save(updatedAccount)
                                        .flatMap(saved -> hashTokenRepository.deleteById(token.id())
                                                .replaceWith(new PasswordResetDetails(now)));
                            });
                });
    }
}
