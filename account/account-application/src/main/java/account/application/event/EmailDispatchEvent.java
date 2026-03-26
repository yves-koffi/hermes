package account.application.event;

import account.domain.model.TokenType;

public record EmailDispatchEvent(
        String recipient,
        TokenType tokenType,
        String language,
        String tokenValue
) {
    public static final String ADDRESS = "account.email.dispatch";
}
