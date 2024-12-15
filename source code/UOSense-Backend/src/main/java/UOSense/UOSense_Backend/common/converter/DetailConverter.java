package UOSense.UOSense_Backend.common.converter;

import UOSense.UOSense_Backend.common.enumClass.Detail;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class DetailConverter extends EnumBaseConverter<Detail> {
    public DetailConverter() {
        super(Detail.class) ;
    }
}
