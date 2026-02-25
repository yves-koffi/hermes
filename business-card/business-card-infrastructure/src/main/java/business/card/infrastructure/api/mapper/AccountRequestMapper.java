package business.card.infrastructure.api.mapper;

import business.card.application.command.LoginCommand;
import business.card.application.result.AccountDetails;
import business.card.application.result.AccountSummary;
import business.card.infrastructure.api.dto.AccountResponse;
import business.card.infrastructure.api.dto.LoginRequest;
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
