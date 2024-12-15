package UOSense.UOSense_Backend.dto;

import UOSense.UOSense_Backend.entity.Menu;
import UOSense.UOSense_Backend.entity.PurposeMenu;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class PurposeMenuResponse {
    private int id;

    private int restaurantId;

    private String name;

    private int price;

    private String imageUrl;

    public static PurposeMenuResponse from(PurposeMenu menu) {
        return new PurposeMenuResponse(
                menu.getId(),
                menu.getRestaurant().getId(),
                menu.getName(),
                menu.getPrice(),
                menu.getImageUrl()
        );
    }
}
