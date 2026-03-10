package account.application.command;

import account.domain.model.PhoneNumber;

public record UpdateAccountCommand(
        String name,
        PhoneNumber phoneNumber
) {
}
