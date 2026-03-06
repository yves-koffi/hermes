package account.infrastructure.api.mapper;

import account.application.command.LoginCommand;
import account.application.result.AccountDetails;
import account.application.result.AccountSummary;
import account.infrastructure.api.dto.AccountResponse;
import account.infrastructure.api.dto.LoginRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "cdi")
public interface AccountRequestMapper {

    LoginCommand toCommand(LoginRequest request);

    @Mapping(target = "token", expression = "java((String) null)")
    @Mapping(target = "photoUrl", expression = "java((String) null)")
    @Mapping(target = "created_at", expression = "java((Integer) null)")
    AccountResponse toResponse(AccountSummary summary);

    @Mapping(target = "token", expression = "java((String) null)")
    AccountResponse toResponse(AccountDetails details);
}
