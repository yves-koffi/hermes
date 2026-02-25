package business.card.infrastructure.persistence.mapper;

import business.card.domain.model.Account;
import business.card.infrastructure.persistence.entity.AccountEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = "cdi")
public interface AccountMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "avatarUrl", source = "avatarUrl")
    @Mapping(target = "googleId", source = "googleId")
    @Mapping(target = "appleId", source = "appleId")
    @Mapping(target = "activatedAt", source = "activatedAt")
    @Mapping(target = "provider", source = "provider")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    Account toDomain(AccountEntity entity);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "avatarUrl", source = "avatarUrl")
    @Mapping(target = "googleId", source = "googleId")
    @Mapping(target = "appleId", source = "appleId")
    @Mapping(target = "activatedAt", source = "activatedAt")
    @Mapping(target = "provider", source = "provider")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    AccountEntity toEntity(Account domain);

    default LocalDateTime map(OffsetDateTime value) {
        return value == null ? null : value.toLocalDateTime();
    }

    default OffsetDateTime map(LocalDateTime value) {
        return value == null ? null : value.atOffset(ZoneOffset.UTC);
    }
}
