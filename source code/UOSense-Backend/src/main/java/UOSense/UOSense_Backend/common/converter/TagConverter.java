package UOSense.UOSense_Backend.common.converter;

import UOSense.UOSense_Backend.common.enumClass.Tag;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TagConverter extends EnumBaseConverter<Tag> {
    public TagConverter() {
        super(Tag.class) ;
    }
}
