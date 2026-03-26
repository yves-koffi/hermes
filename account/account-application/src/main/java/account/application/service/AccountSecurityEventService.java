package account.application.service;

import account.application.spi.AccountSecurityEventRepository;
import account.domain.model.Account;
import account.domain.model.AccountSecurityEvent;
import account.domain.model.AccountSecurityEventType;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import shared.application.context.RequestContext;

import java.time.OffsetDateTime;
import java.util.UUID;

@ApplicationScoped
public class AccountSecurityEventService {

    @Inject
    AccountSecurityEventRepository accountSecurityEventRepository;
    @Inject
    RequestContext context;

    public Uni<Void> record(Account account, AccountSecurityEventType eventType, String detail) {
        return record(account.id(), eventType, detail);
    }

    public Uni<Void> record(UUID accountId, AccountSecurityEventType eventType, String detail) {
        AccountSecurityEvent event = new AccountSecurityEvent(
                UUID.randomUUID(),
                accountId,
                eventType,
                detail,
                context.getExecutionContext() == null ? null : context.getExecutionContext().ip(),
                OffsetDateTime.now()
        );
        return accountSecurityEventRepository.save(event).replaceWithVoid();
    }
}
