package business.card.infrastructure.api.mapper;

import business.card.application.command.BusinessCardCommand;
import business.card.application.result.BusinessCardDetails;
import business.card.domain.model.BusinessCardStatus;
import business.card.infrastructure.api.dto.BusinessCardRequest;
import business.card.infrastructure.api.dto.BusinessCardResponse;
import com.fasterxml.jackson.databind.JsonNode;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "cdi")
public interface BusinessCardRequestMapper {

    @Mapping(target = "status", source = "status", qualifiedByName = "toStatus")
    BusinessCardCommand toCommand(BusinessCardRequest request);

    @Mapping(target = "status", source = "status", qualifiedByName = "toStatusString")
    BusinessCardRequest toRequest(BusinessCardCommand command);

    @Mapping(target = "avatarUrl", source = "avatarUrl")
    @Mapping(target = "raw", source = "raw")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "bin", source = "bin")
    @Mapping(target = "saveAt", source = "saveAt")
    @Mapping(target = "uid", source = "uid")
    @Mapping(target = "status", source = "status", qualifiedByName = "normalizeStatus")
    BusinessCardResponse toResponse(BusinessCardDetails details);

    List<BusinessCardCommand> toCommands(List<BusinessCardRequest> requests);

    List<BusinessCardResponse> toResponses(List<BusinessCardDetails> details);

    @Named("toStatus")
    default BusinessCardStatus toStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return BusinessCardStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    @Named("toStatusString")
    default String toStatusString(BusinessCardStatus status) {
        if (status == null) {
            return null;
        }
        return status.name().toLowerCase();
    }

    @Named("normalizeStatus")
    default String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        return status.trim().toLowerCase();
    }

    default String map(JsonNode raw) {
        return raw == null ? null : raw.toString();
    }

    default Integer map(Boolean bin) {
        if (bin == null) {
            return null;
        }
        return bin ? 1 : 0;
    }
}
