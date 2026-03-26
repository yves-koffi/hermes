package account.infrastructure.email;

import account.application.event.EmailDispatchEvent;
import account.domain.model.RenderedEmail;
import account.domain.model.EmailType;
import account.domain.model.TokenType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@ApplicationScoped
public class EmailContentFactory {

    @Inject
    EmailTemplateTypeResolver emailTemplateTypeResolver;
    @Inject
    EmailMessageSource emailMessageSource;

    @ConfigProperty(name = "account.email.validation-url", defaultValue = "http://localhost:8080/account/verify-email")
    String verificationUrl;

    @ConfigProperty(name = "account.email.password-reset-url", defaultValue = "http://localhost:8080/account/reset-password")
    String passwordResetUrl;

    public RenderedEmail render(EmailDispatchEvent event) {
        TokenType tokenType = event.tokenType();
        EmailType emailType = emailTemplateTypeResolver.resolve(tokenType);
        String language = normalizeLanguage(event.language());
        String keyPrefix = emailTemplateTypeResolver.messageKeyPrefix(emailType, tokenType);
        String subject = emailMessageSource.message(emailTemplateTypeResolver.subject(emailType, tokenType, language), language);

        if (tokenType == TokenType.EMAIL_VERIFICATION_CODE || tokenType == TokenType.PASSWORD_RESET_CODE) {
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
                    .replace("{{code}}", event.tokenValue());
            return new RenderedEmail(subject, html);
        }

        String baseUrl = tokenType == TokenType.EMAIL_VERIFICATION_LINK ? verificationUrl : passwordResetUrl;
        String link = buildLink(baseUrl, event.tokenValue());
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
                .replace("{{token}}", event.tokenValue())
                .replace("{{validation_url}}", link);
        return new RenderedEmail(subject, html);
    }

    private String buildLink(String baseUrl, String token) {
        String encoded = URLEncoder.encode(token, StandardCharsets.UTF_8);
        if (baseUrl.contains("{token}")) {
            return baseUrl.replace("{token}", encoded);
        }
        if (baseUrl.contains("token=")) {
            return baseUrl + encoded;
        }
        char separator = baseUrl.contains("?") ? '&' : '?';
        return baseUrl + separator + "token=" + encoded;
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

    private String normalizeLanguage(String language) {
        return language == null || language.isBlank() ? "fr" : language;
    }
}
