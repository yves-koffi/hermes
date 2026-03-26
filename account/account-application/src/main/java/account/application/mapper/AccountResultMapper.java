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
                account.id(),
                account.name(),
                account.provider(),
                account.activatedAt()
        );
    }

    public AccountDetails toDetails(Account account) {
        if (account == null) {
            return null;
        }
        return new AccountDetails(
                account.id(),
                account.name(),
                account.email(),
                account.phoneNumber(),
                account.avatarUrl(),
                account.provider(),
                account.activatedAt(),
                account.disabledAt(),
                account.createdAt(),
                account.updatedAt()
        );
    }
}
