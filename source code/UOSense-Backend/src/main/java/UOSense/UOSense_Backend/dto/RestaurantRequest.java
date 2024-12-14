package UOSense.UOSense_Backend.dto;

import UOSense.UOSense_Backend.common.enumClass.Category;
import UOSense.UOSense_Backend.common.enumClass.DoorType;
import UOSense.UOSense_Backend.common.enumClass.SubDescription;
import UOSense.UOSense_Backend.entity.Restaurant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class RestaurantRequest {
    private int id;

    private String name;

    private DoorType doorType;

    private double latitude;

    private double longitude;

    private String address;

    private String phoneNumber;

    private Category category;

    private SubDescription subDescription;

    private String description;

    public static Restaurant toEntity(RestaurantRequest restaurantRequest) {
        Restaurant restaurant = Restaurant.builder()
                .name(restaurantRequest.getName())
                .doorType(restaurantRequest.getDoorType())
                .longitude(restaurantRequest.getLongitude())
                .latitude(restaurantRequest.getLatitude())
                .address(restaurantRequest.getAddress())
                .phoneNumber(restaurantRequest.getPhoneNumber())
                .category(restaurantRequest.getCategory())
                .subDescription(restaurantRequest.getSubDescription())
                .description(restaurantRequest.getDescription())
                .build();

        if (restaurantRequest.getId() == -1) {
            // 신규 엔티티이므로 id 필드를 비워둠.
            return restaurant;
        }
        else {
            restaurant.setId(restaurantRequest.getId());
            return restaurant;
        }
    }
}
