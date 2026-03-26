package account.application.mapper;

import account.application.command.CreateAccountCommand;
import account.application.command.RegisterCommand;
import account.domain.model.Account;
import account.domain.model.PhoneNumber;
import account.domain.model.Provider;
import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.UUID;

@ApplicationScoped
public class AccountCommandMapper {

    public Account toAccount(RegisterCommand command) {
        OffsetDateTime now = OffsetDateTime.now();
        return new Account(
                UUID.randomUUID(),
                command.name(),
                normalizeEmail(command.email()),
                command.phoneNumber(),
                BcryptUtil.bcryptHash(command.password()),
                null,
                null,
                Provider.BASIC,
                command.requiredVerifyEmail() ? null : now,
                null,
                now,
                now
        );
    }

    public Account toAccount(CreateAccountCommand command) {
        OffsetDateTime now = OffsetDateTime.now();
        Provider provider = command.provider() == null ? Provider.BASIC : command.provider();
        return new Account(
                UUID.randomUUID(),
                command.name(),
                normalizeEmail(command.email()),
                toPhoneNumber(command.prefix(), command.number()),
                command.password() == null ? null : BcryptUtil.bcryptHash(command.password()),
                command.avatarUrl(),
                command.providerId(),
                provider,
                provider == Provider.BASIC ? null : now,
                null,
                now,
                now
        );
    }

    private PhoneNumber toPhoneNumber(String prefix, String number) {
        if (prefix == null && number == null) {
            return null;
        }
        return new PhoneNumber(prefix, number);
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
