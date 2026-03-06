package store.purchase.infrastructure.persistence.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import store.purchase.domain.PurchaseType;

@Converter
public class PurchaseTypeConverter implements AttributeConverter<PurchaseType, String> {

    @Override
    public String convertToDatabaseColumn(PurchaseType attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public PurchaseType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : PurchaseType.fromValue(dbData);
    }
}
