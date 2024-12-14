package UOSense.UOSense_Backend.dto;

import UOSense.UOSense_Backend.entity.Menu;
import UOSense.UOSense_Backend.entity.Restaurant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class NewMenuRequest {
    private int restaurantId;
    private String name;
    private int price;
    private String description;

    public Menu toEntity(Restaurant restaurant, String url) {
        Menu.MenuBuilder builder = Menu.builder()
                .restaurant(restaurant)
                .name(this.name)
                .price(this.price)
                .description(this.description);

        if (!url.isEmpty()) {
            builder.imageUrl(url);
        }

        return builder.build();
    }
}
