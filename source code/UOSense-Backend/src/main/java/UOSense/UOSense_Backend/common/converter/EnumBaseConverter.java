package UOSense.UOSense_Backend.common.converter;

import UOSense.UOSense_Backend.common.Utils.EnumUtils;
import UOSense.UOSense_Backend.common.enumClass.BaseEnum;
import jakarta.persistence.AttributeConverter;

public abstract class EnumBaseConverter<E extends Enum<E> & BaseEnum> implements AttributeConverter<E, String> {
    private final Class<E> enumClass;

    protected EnumBaseConverter(Class<E> enumClass) {
        this.enumClass = enumClass;
    }

    @Override
    public String convertToDatabaseColumn(E attribute) {
        return attribute.getValue();
    }

    @Override
    public E convertToEntityAttribute(String dbData) {
        return EnumUtils.fromValue(enumClass, dbData);
    }
}