package account.infrastructure.persistence.mapper;

import account.domain.model.AccountSecurityEvent;
import account.infrastructure.persistence.entity.AccountSecurityEventEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "cdi")
public interface AccountSecurityEventMapper {

    AccountSecurityEvent toDomain(AccountSecurityEventEntity entity);

    AccountSecurityEventEntity toEntity(AccountSecurityEvent domain);
}
