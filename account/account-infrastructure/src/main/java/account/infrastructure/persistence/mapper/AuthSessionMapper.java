package account.infrastructure.persistence.mapper;

import account.domain.model.AuthSession;
import account.infrastructure.persistence.entity.AuthSessionEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "cdi")
public interface AuthSessionMapper {

    AuthSession toDomain(AuthSessionEntity entity);

    AuthSessionEntity toEntity(AuthSession domain);
}
