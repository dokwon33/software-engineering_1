package UOSense.UOSense_Backend.dto;

import UOSense.UOSense_Backend.entity.Restaurant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class RestaurantListResponse {
    private int id;

    private String name;

    private double longitude;

    private double latitude;

    private String doorType;

    private String address;

    private double rating;

    private String category;

    private int reviewCount;

    private int bookmarkCount;

    private String restaurantImage;

    public static RestaurantListResponse from(Restaurant restaurant, String imageUrl) {
        return RestaurantListResponse.builder()
                .id(restaurant.getId())
                .name(restaurant.getName())
                .longitude(restaurant.getLongitude())
                .latitude(restaurant.getLatitude())
                .doorType(restaurant.getDoorType().getValue())
                .address(restaurant.getAddress())
                .rating(restaurant.getRating())
                .category(restaurant.getCategory().getValue())
                .reviewCount(restaurant.getReviewCount())
                .bookmarkCount(restaurant.getBookmarkCount())
                .restaurantImage(imageUrl)
                .build();
    }
}
