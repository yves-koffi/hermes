package business.card.infrastructure.persistence.entity;

import business.card.domain.model.BusinessCardType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class BusinessCardTypeConverter implements AttributeConverter<BusinessCardType, String> {

    @Override
    public String convertToDatabaseColumn(BusinessCardType attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name().toLowerCase();
    }

    @Override
    public BusinessCardType convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return BusinessCardType.valueOf(dbData.toUpperCase());
    }
}
