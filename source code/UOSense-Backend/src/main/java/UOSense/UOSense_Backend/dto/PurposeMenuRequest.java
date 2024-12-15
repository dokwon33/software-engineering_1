package UOSense.UOSense_Backend.dto;

import UOSense.UOSense_Backend.entity.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class PurposeMenuRequest {
    private int restaurantId;

    private String name;

    private int price;

    private int userId;

    public PurposeMenu toEntity(Restaurant purposeRest, User proposer, String url) {
        PurposeMenu.PurposeMenuBuilder builder = PurposeMenu.builder()
                .restaurant(purposeRest)
                .name(this.name)
                .price(this.price)
                .imageUrl(url)
                .user(proposer);

        if (!url.isEmpty()) {
            builder.imageUrl(url);
        }

        return builder.build();
    }
}