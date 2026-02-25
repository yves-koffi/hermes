package life.ping.infrastructure.persistence.mapper;

import life.ping.domain.model.Account;
import life.ping.infrastructure.persistence.entity.AccountEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "cdi")
public interface AccountMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "userName", source = "userName")
    @Mapping(target = "appUuid", source = "appUuid")
    @Mapping(target = "deviceUniqueId", source = "deviceUniqueId")
    @Mapping(target = "deviceModel", source = "deviceModel")
    @Mapping(target = "devicePlatform", source = "devicePlatform")
    @Mapping(target = "timezone", source = "timezone")
    @Mapping(target = "callbackTime", source = "callbackTime")
    @Mapping(target = "checkInFrequency", source = "checkInFrequency")
    @Mapping(target = "thresholdPeriod", source = "thresholdPeriod")
    @Mapping(target = "lastCheckinAt", source = "lastCheckinAt")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    Account toDomain(AccountEntity entity);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "userName", source = "userName")
    @Mapping(target = "appUuid", source = "appUuid")
    @Mapping(target = "deviceUniqueId", source = "deviceUniqueId")
    @Mapping(target = "deviceModel", source = "deviceModel")
    @Mapping(target = "devicePlatform", source = "devicePlatform")
    @Mapping(target = "timezone", source = "timezone")
    @Mapping(target = "callbackTime", source = "callbackTime")
    @Mapping(target = "checkInFrequency", source = "checkInFrequency")
    @Mapping(target = "thresholdPeriod", source = "thresholdPeriod")
    @Mapping(target = "lastCheckinAt", source = "lastCheckinAt")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    AccountEntity toEntity(Account domain);
}
