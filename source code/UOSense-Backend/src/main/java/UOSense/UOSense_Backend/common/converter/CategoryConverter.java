package UOSense.UOSense_Backend.common.converter;

import UOSense.UOSense_Backend.common.enumClass.Category;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class CategoryConverter extends EnumBaseConverter<Category> {
    public CategoryConverter() {
        super(Category.class);
    }
}
