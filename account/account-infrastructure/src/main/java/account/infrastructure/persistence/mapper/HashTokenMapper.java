package account.infrastructure.persistence.mapper;

import account.domain.model.HashToken;
import account.infrastructure.persistence.entity.HashTokenEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "cdi")
public interface HashTokenMapper {

    HashToken toDomain(HashTokenEntity entity);

    HashTokenEntity toEntity(HashToken domain);
}
