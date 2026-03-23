package account.application.service;

import account.application.command.SendVerifyCodeCommand;
import account.application.result.VerifyCodeSentDetails;
import account.application.spi.AccountRepository;
import account.application.spi.HashTokenRepository;
import account.application.usecase.VerificationNotificationUseCase;
import account.domain.model.EmailType;
import account.domain.model.HashToken;
import account.domain.model.TokenType;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.MailerName;
import io.quarkus.mailer.reactive.ReactiveMailer;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import shared.application.context.RequestContext;
import shared.domain.exception.DomainConflictException;
import shared.domain.exception.DomainNotFoundException;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

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
    @MailerName("account")
    ReactiveMailer mailer;
    @Inject
    RequestContext context;
    @Inject
    EmailTemplateTypeResolver emailTemplateTypeResolver;
    @Inject
    EmailMessageSource emailMessageSource;

    @ConfigProperty(name = "account.email.validation-url", defaultValue = "http://localhost:8080/account/verify-email")
    String validationUrl;

    @Override
    public Uni<VerifyCodeSentDetails> execute(SendVerifyCodeCommand command) {
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

        return accountRepository.findByEmail(command.email())
                .flatMap(accountOpt -> {
                    if (accountOpt.isEmpty()) {
                        return Uni.createFrom().failure(
                                new DomainNotFoundException(
                                        "ACCOUNT_NOT_FOUND",
                                        "account.not_found",
                                        Map.of("email", command.email())
                                )
                        );
                    }

                    var account = accountOpt.get();
                    if (account.isActivated()) {
                        return Uni.createFrom().failure(
                                new DomainConflictException(
                                        "ACCOUNT_ALREADY_VERIFIED",
                                        "account.already_verified",
                                        Map.of("email", account.email())
                                )
                        );
                    }

                    String tokenValue = generateToken(tokenType);
                    HashToken hashToken = new HashToken(
                            UUID.randomUUID(),
                            hash(tokenValue),
                            tokenType,
                            account.id(),
                            computeExpiryDate(tokenType),
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
                            .flatMap(saved -> sendEmail(account.email(), tokenType, tokenValue))
                            .replaceWith(new VerifyCodeSentDetails(true));
                });
    }

    private Uni<Void> sendEmail(String recipient, TokenType tokenType, String value) {
        EmailType emailType = emailTemplateTypeResolver.resolve(tokenType);
        String language = context.getExecutionContext() != null ? context.getExecutionContext().language() : "fr";
        String keyPrefix = emailTemplateTypeResolver.messageKeyPrefix(emailType, tokenType);
        if (tokenType == TokenType.EMAIL_VERIFICATION_CODE || tokenType == TokenType.VERIFY_CODE) {
            String html = loadTemplate(emailTemplateTypeResolver.codeTemplatePath(emailType, language))
                    .replace("{{lang}}", language)
                    .replace("{{email_title}}", emailMessageSource.message(keyPrefix + ".subject", language))
                    .replace("{{email_badge}}", emailMessageSource.message(keyPrefix + ".badge", language))
                    .replace("{{email_heading}}", emailMessageSource.message(keyPrefix + ".heading", language))
                    .replace("{{email_greeting}}", emailMessageSource.message("email.common.greeting", language))
                    .replace("{{email_message}}", emailMessageSource.message(keyPrefix + ".message", language))
                    .replace("{{email_code_label}}", emailMessageSource.message(keyPrefix + ".code_label", language))
                    .replace("{{email_helper}}", emailMessageSource.message(keyPrefix + ".helper", language))
                    .replace("{{email_footer}}", emailMessageSource.message(keyPrefix + ".footer", language))
                    .replace("{{email_disclaimer}}", emailMessageSource.message(keyPrefix + ".disclaimer", language))
                    .replace("{{code}}", value);
            return mailer.send(Mail.withHtml(recipient, emailMessageSource.message(emailTemplateTypeResolver.subject(emailType, tokenType, language), language), html));
        }

        String link = buildValidationLink(value);
        String html = loadTemplate(emailTemplateTypeResolver.linkTemplatePath(emailType, language))
                .replace("{{lang}}", language)
                .replace("{{email_title}}", emailMessageSource.message(keyPrefix + ".subject", language))
                .replace("{{email_badge}}", emailMessageSource.message(keyPrefix + ".badge", language))
                .replace("{{email_heading}}", emailMessageSource.message(keyPrefix + ".heading", language))
                .replace("{{email_greeting}}", emailMessageSource.message("email.common.greeting", language))
                .replace("{{email_message}}", emailMessageSource.message(keyPrefix + ".message", language))
                .replace("{{email_action_label}}", emailMessageSource.message(keyPrefix + ".action_label", language))
                .replace("{{email_helper}}", emailMessageSource.message(keyPrefix + ".helper", language))
                .replace("{{email_footer}}", emailMessageSource.message(keyPrefix + ".footer", language))
                .replace("{{token}}", value)
                .replace("{{validation_url}}", link);
        return mailer.send(Mail.withHtml(recipient, emailMessageSource.message(emailTemplateTypeResolver.subject(emailType, tokenType, language), language), html));
    }

    private String generateToken(TokenType tokenType) {
        return tokenType == TokenType.EMAIL_VERIFICATION_CODE || tokenType == TokenType.VERIFY_CODE
                ? String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1_000_000))
                : UUID.randomUUID().toString();
    }

    private OffsetDateTime computeExpiryDate(TokenType tokenType) {
        OffsetDateTime now = OffsetDateTime.now();
        return switch (tokenType) {
            case VERIFY_CODE -> now.plusMinutes(10);
            case VERIFY_TOKEN -> now.plusHours(24);
            case EMAIL_VERIFICATION_CODE -> now.plusMinutes(10);
            case EMAIL_VERIFICATION_LINK -> now.plusHours(24);
            case PASSWORD_RESET_CODE -> now.plusMinutes(10);
            case PASSWORD_RESET_LINK -> now.plusHours(24);
            case SESSION_REFRESH -> now.plusDays(30);
            case REFRESH_TOKEN -> now.plusDays(30);
        };
    }

    private String buildValidationLink(String token) {
        String encoded = URLEncoder.encode(token, StandardCharsets.UTF_8);
        if (validationUrl.contains("{token}")) {
            return validationUrl.replace("{token}", encoded);
        }
        if (validationUrl.contains("token=")) {
            return validationUrl + encoded;
        }
        char separator = validationUrl.contains("?") ? '&' : '?';
        return validationUrl + separator + "token=" + encoded;
    }

    private String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(rawToken.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    private String loadTemplate(String path) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try (InputStream stream = classLoader.getResourceAsStream(path)) {
            if (stream == null) {
                throw new IllegalStateException("Missing email template: " + path);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
