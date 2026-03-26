package account.application.usecase;

import account.application.result.AccountSecurityEventResult;
import io.smallrye.mutiny.Uni;

import java.util.List;

public interface SecurityHistoryUseCase {
    Uni<List<AccountSecurityEventResult>> execute();
}
