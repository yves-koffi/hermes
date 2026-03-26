package account.application.spi;

import account.domain.model.AccountSecurityEvent;
import io.smallrye.mutiny.Uni;

import java.util.List;
import java.util.UUID;

public interface AccountSecurityEventRepository {

    Uni<AccountSecurityEvent> save(AccountSecurityEvent event);

    Uni<List<AccountSecurityEvent>> findRecentByAccountId(UUID accountId, int limit);
}
