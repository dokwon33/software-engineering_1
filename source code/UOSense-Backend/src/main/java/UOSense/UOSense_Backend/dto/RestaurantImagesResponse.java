package UOSense.UOSense_Backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class RestaurantImagesResponse {
    private int restaurantId;
    private List<ImageInfo> ImageList;
}
