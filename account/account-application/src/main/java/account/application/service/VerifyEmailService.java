package account.application.service;

import account.application.command.VerifyAccountCommand;
import account.application.result.AccountVerificationDetails;
import account.application.spi.AccountRepository;
import account.application.spi.HashTokenRepository;
import account.application.usecase.VerifyEmailUseCase;
import account.domain.model.Account;
import account.domain.model.HashToken;
import account.domain.model.TokenType;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import shared.domain.exception.DomainConflictException;
import shared.domain.exception.DomainNotFoundException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.HexFormat;
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
public class VerifyEmailService implements VerifyEmailUseCase {

    @Inject
    HashTokenRepository hashTokenRepository;
    @Inject
    AccountRepository accountRepository;

    @Override
    public Uni<AccountVerificationDetails> execute(VerifyAccountCommand command) {
        if (command.token() == null || command.token().isBlank()) {
            return Uni.createFrom().failure(
                    new DomainConflictException(
                            "INVALID_VERIFY_TOKEN",
                            "account.verify_token.invalid",
                            Map.of()
                    )
            );
        }

        String hashToken = hash(command.token());
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
                                            .replaceWith(new AccountVerificationDetails(current.activatedAt()));
                                }
                                OffsetDateTime verifiedAt = current.activatedAt() == null
                                        ? OffsetDateTime.now()
                                        : current.activatedAt();

                                Account activated = new Account(
                                        current.id(),
                                        current.name(),
                                        current.email(),
                                        current.phoneNumber(),
                                        current.password(),
                                        current.avatarUrl(),
                                        current.providerId(),
                                        current.provider(),
                                        verifiedAt,
                                        current.createdAt(),
                                        OffsetDateTime.now()
                                );

                                return accountRepository.save(activated)
                                        .flatMap(saved -> hashTokenRepository.deleteById(token.id())
                                                .replaceWith(new AccountVerificationDetails(saved.activatedAt())));
                            });
                });
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
