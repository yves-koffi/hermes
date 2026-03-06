package account.application.mapper;

import account.application.result.AccountDetails;
import account.application.result.AccountSummary;
import account.domain.model.Account;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AccountResultMapper {

    public AccountSummary toSummary(Account account) {
        if (account == null) {
            return null;
        }
        return new AccountSummary(
                account.id() != null ? account.id().toString() : null,
                account.name(),
                account.email(),
                account.provider() != null ? account.provider() : null,
                account.isActivated()
        );
    }

    public AccountDetails toDetails(Account account) {
        if (account == null) {
            return null;
        }
        return new AccountDetails(
                account.name(),
                account.email(),
                account.provider() != null ? account.provider().name().toLowerCase() : null,
                account.avatarUrl(),
                account.isActivated(),
                account.createdAt() != null ? Math.toIntExact(account.createdAt().toEpochSecond()) : null
        );
    }
}
