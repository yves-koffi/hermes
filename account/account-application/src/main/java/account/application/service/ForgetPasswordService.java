package account.application.service;

import account.application.command.ForgetPasswordCommand;
import account.application.result.ForgetPasswordRequestDetails;
import account.application.spi.AccountRepository;
import account.application.spi.HashTokenRepository;
import account.application.usecase.ForgetPasswordUseCase;
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
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@ApplicationScoped
public class ForgetPasswordService implements ForgetPasswordUseCase {

    private static final String CODE_TEMPLATE_PATH = "templates/email/code_template.html";
    private static final String TOKEN_TEMPLATE_PATH = "templates/email/token_template.html";

    @Inject
    AccountRepository accountRepository;
    @Inject
    HashTokenRepository hashTokenRepository;
    @Inject
    @MailerName("account")
    ReactiveMailer mailer;
    @Inject
    RequestContext context;

    @ConfigProperty(name = "account.email.validation-url", defaultValue = "http://localhost:8080/account/verify-email")
    String validationUrl;

    @Override
    public Uni<ForgetPasswordRequestDetails> execute(ForgetPasswordCommand command) {
        if (command.email() == null || command.email().isBlank()) {
            return Uni.createFrom().failure(
                    new DomainConflictException(
                            "INVALID_EMAIL",
                            "account.email.invalid",
                            Map.of()
                    )
            );
        }

        TokenType tokenType = command.type() == null ? TokenType.VERIFY_CODE : command.type();

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

                    String tokenValue = generateToken(tokenType);
                    HashToken hashToken = new HashToken(
                            UUID.randomUUID(),
                            hash(tokenValue),
                            tokenType,
                            accountOpt.get().id(),
                            computeExpiryDate(tokenType),
                            context.getExecutionContext() != null ? context.getExecutionContext().ip() : null,
                            null,
                            null,
                            null
                    );

                    return hashTokenRepository.save(hashToken)
                            .flatMap(saved -> sendEmail(accountOpt.get().email(), tokenType, tokenValue))
                            .replaceWith(new ForgetPasswordRequestDetails(accountOpt.get().email()));
                });
    }

    private Uni<Void> sendEmail(String recipient, TokenType tokenType, String value) {
        if (tokenType == TokenType.VERIFY_CODE) {
            String html = loadTemplate(CODE_TEMPLATE_PATH)
                    .replace("{{code}}", value);
            return mailer.send(Mail.withHtml(recipient, "Code de verification", html));
        }

        String link = buildValidationLink(value);
        String html = loadTemplate(TOKEN_TEMPLATE_PATH)
                .replace("{{token}}", value)
                .replace("{{validation_url}}", link);
        return mailer.send(Mail.withHtml(recipient, "Lien de verification", html));
    }

    private String generateToken(TokenType tokenType) {
        return tokenType == TokenType.VERIFY_CODE
                ? String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1_000_000))
                : UUID.randomUUID().toString();
    }

    private OffsetDateTime computeExpiryDate(TokenType tokenType) {
        OffsetDateTime now = OffsetDateTime.now();
        return switch (tokenType) {
            case VERIFY_CODE -> now.plusMinutes(10);
            case VERIFY_TOKEN -> now.plusHours(24);
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
