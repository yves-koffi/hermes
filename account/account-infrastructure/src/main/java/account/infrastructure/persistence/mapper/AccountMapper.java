package account.infrastructure.persistence.mapper;

import account.domain.model.Account;
import account.domain.model.PhoneNumber;
import account.infrastructure.persistence.entity.AccountEntity;
import account.infrastructure.persistence.entity.PhoneNumberEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "cdi")
public interface AccountMapper {

    @Mapping(source = "phoneNumberEntity", target = "phoneNumber")
    Account toDomain(AccountEntity entity);

    @Mapping(source = "phoneNumber", target = "phoneNumberEntity")
    AccountEntity toEntity(Account domain);

    default PhoneNumber toDomain(PhoneNumberEntity value) {
        return value == null ? null : new PhoneNumber(value.prefix(), value.number());
    }

    default PhoneNumberEntity toEntity(PhoneNumber value) {
        return value == null ? null : new PhoneNumberEntity(value.prefix(), value.number());
    }
}
