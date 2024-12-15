package UOSense.UOSense_Backend.common.converter;

import UOSense.UOSense_Backend.common.enumClass.DoorType;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class DoorTypeConverter extends EnumBaseConverter<DoorType> {
    public DoorTypeConverter() {
        super(DoorType.class);
    }
}
