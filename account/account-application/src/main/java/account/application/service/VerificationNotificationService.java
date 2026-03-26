package account.application.service;

import account.application.command.SendVerifyCodeCommand;
import account.application.event.EmailDispatchEvent;
import account.application.result.VerifyCodeSentResult;
import account.application.spi.AccountRepository;
import account.application.spi.HashTokenRepository;
import account.application.spi.NotificationByEmailDispatchPublisher;
import account.application.usecase.VerificationNotificationUseCase;
import account.domain.model.HashToken;
import account.domain.model.TokenType;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import shared.application.context.RequestContext;
import shared.domain.exception.DomainConflictException;

import java.util.Locale;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Implémentation du use case d'envoi de notification de vérification email.
 *
 * Le service contrôle que le compte existe encore et qu'il n'est pas déjà activé. Il supprime
 * d'abord les anciens tokens de vérification actifs pour ce compte, génère ensuite un nouveau
 * code ou lien de vérification, stocke uniquement son hash et déclenche l'envoi de l'email
 * correspondant à l'aide des templates HTML de l'application.
 */
@ApplicationScoped
public class VerificationNotificationService implements VerificationNotificationUseCase {

    @Inject
    AccountRepository accountRepository;
    @Inject
    HashTokenRepository hashTokenRepository;
    @Inject
    RequestContext context;
    @Inject
    OneTimeTokenService oneTimeTokenService;
    @Inject
    NotificationByEmailDispatchPublisher emailDispatchPublisher;

    @Override
    public Uni<VerifyCodeSentResult> execute(SendVerifyCodeCommand command) {
        if (command.email() == null || command.email().isBlank()) {
            return Uni.createFrom().failure(
                    new DomainConflictException(
                            "INVALID_EMAIL",
                            "account.email.invalid",
                            Map.of()
                    )
            );
        }

        TokenType tokenType = command.type() == null ? TokenType.EMAIL_VERIFICATION_CODE : command.type();
        if (tokenType != TokenType.EMAIL_VERIFICATION_CODE && tokenType != TokenType.EMAIL_VERIFICATION_LINK) {
            return Uni.createFrom().failure(
                    new DomainConflictException(
                            "INVALID_VERIFY_TOKEN_TYPE",
                            "account.verify_token.invalid_type",
                            Map.of("type", tokenType.name())
                    )
            );
        }

        String normalizedEmail = normalizeEmail(command.email());
        return accountRepository.findByEmail(normalizedEmail)
                .flatMap(accountOpt -> {
                    if (accountOpt.isEmpty()) {
                        return Uni.createFrom().item(new VerifyCodeSentResult(
                                true,
                                "EMAIL",
                                tokenType,
                                null
                        ));
                    }

                    var account = accountOpt.get();
                    if (account.isActivated()) {
                        return Uni.createFrom().item(new VerifyCodeSentResult(
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
                            account.id(),
                            oneTimeTokenService.computeExpiryDate(tokenType),
                            context.getExecutionContext() != null ? context.getExecutionContext().ip() : null,
                            null,
                            null,
                            null
                    );

                    return hashTokenRepository.deleteByAccountIdAndTokenTypes(
                                    account.id(),
                                    List.of(TokenType.EMAIL_VERIFICATION_CODE, TokenType.EMAIL_VERIFICATION_LINK)
                            )
                            .flatMap(ignored -> hashTokenRepository.save(hashToken))
                            .flatMap(saved -> emailDispatchPublisher.publish(new EmailDispatchEvent(
                                    account.email(),
                                    tokenType,
                                    resolveLanguage(),
                                    tokenValue
                            )))
                            .replaceWith(new VerifyCodeSentResult(
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
