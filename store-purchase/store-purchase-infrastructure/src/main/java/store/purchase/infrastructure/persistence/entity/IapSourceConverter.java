package store.purchase.infrastructure.persistence.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import store.purchase.domain.IapSource;

@Converter
public class IapSourceConverter implements AttributeConverter<IapSource, String> {

    @Override
    public String convertToDatabaseColumn(IapSource attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public IapSource convertToEntityAttribute(String dbData) {
        return dbData == null ? null : IapSource.fromValue(dbData);
    }
}
