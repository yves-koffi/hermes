package business.card.infrastructure.persistence.entity;

import business.card.domain.model.Provider;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class ProviderConverter implements AttributeConverter<Provider, String> {

    @Override
    public String convertToDatabaseColumn(Provider attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name().toLowerCase();
    }

    @Override
    public Provider convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return Provider.valueOf(dbData.toUpperCase());
    }
}
