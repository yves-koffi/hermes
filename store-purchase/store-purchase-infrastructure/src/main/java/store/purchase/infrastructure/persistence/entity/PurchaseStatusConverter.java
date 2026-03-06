package store.purchase.infrastructure.persistence.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import store.purchase.domain.PurchaseStatus;

@Converter
public class PurchaseStatusConverter implements AttributeConverter<PurchaseStatus, String> {

    @Override
    public String convertToDatabaseColumn(PurchaseStatus attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public PurchaseStatus convertToEntityAttribute(String dbData) {
        return dbData == null ? null : PurchaseStatus.fromValue(dbData);
    }
}
