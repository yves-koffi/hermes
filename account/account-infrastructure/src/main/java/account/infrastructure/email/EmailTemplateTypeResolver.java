package account.infrastructure.email;

import account.domain.model.EmailType;
import account.domain.model.TokenType;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class EmailTemplateTypeResolver {

    private static final String DEFAULT_LANGUAGE = "fr";

    public EmailType resolve(TokenType tokenType) {
        return switch (tokenType) {
            case EMAIL_VERIFICATION_CODE, EMAIL_VERIFICATION_LINK -> EmailType.EMAIL_VERIFICATION;
            case PASSWORD_RESET_CODE, PASSWORD_RESET_LINK -> EmailType.PASSWORD_RESET;
            default -> throw new IllegalArgumentException("Unsupported email token type: " + tokenType);
        };
    }

    public String subject(EmailType emailType, TokenType tokenType, String language) {
        return messageKeyPrefix(emailType, tokenType) + ".subject";
    }

    public String codeTemplatePath(EmailType emailType, String language) {
        return switch (emailType) {
            case EMAIL_VERIFICATION -> "templates/email/email_verification_code_template.html";
            case PASSWORD_RESET -> "templates/email/password_reset_code_template.html";
        };
    }

    public String linkTemplatePath(EmailType emailType, String language) {
        return switch (emailType) {
            case EMAIL_VERIFICATION -> "templates/email/email_verification_link_template.html";
            case PASSWORD_RESET -> "templates/email/password_reset_link_template.html";
        };
    }

    public String messageKeyPrefix(EmailType emailType, TokenType tokenType) {
        return switch (emailType) {
            case EMAIL_VERIFICATION -> isCode(tokenType) ? "email.email_verification.code" : "email.email_verification.link";
            case PASSWORD_RESET -> isCode(tokenType) ? "email.password_reset.code" : "email.password_reset.link";
        };
    }

    private boolean isCode(TokenType tokenType) {
        return tokenType == TokenType.EMAIL_VERIFICATION_CODE
                || tokenType == TokenType.PASSWORD_RESET_CODE;
    }
}
