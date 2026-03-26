package account.application.mapper;

import account.application.command.RegisterCommand;
import account.domain.model.Account;
import account.domain.model.PhoneNumber;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class AccountCommandMapperTest {

    @Test
    void should_leave_account_unactivated_when_email_verification_is_required() {
        AccountCommandMapper mapper = new AccountCommandMapper();

        Account account = mapper.toAccount(new RegisterCommand(
                "John",
                "john@example.com",
                "secret",
                new PhoneNumber("+225", "0700000000"),
                true
        ));

        assertNull(account.activatedAt());
    }

    @Test
    void should_activate_account_when_email_verification_is_not_required() {
        AccountCommandMapper mapper = new AccountCommandMapper();

        Account account = mapper.toAccount(new RegisterCommand(
                "John",
                "john@example.com",
                "secret",
                new PhoneNumber("+225", "0700000000"),
                false
        ));

        assertNotNull(account.activatedAt());
    }
}
