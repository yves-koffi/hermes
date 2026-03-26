package account.application.command;

import account.domain.model.PhoneNumber;

public record RegisterCommand(
        String name,
        String email,
        String password,
        PhoneNumber phoneNumber,
        boolean requiredVerifyEmail
) {
}
