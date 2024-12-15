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
public class RestaurantResponse {
    private int id;

    private String name;

    private String doorType;

    private double latitude;

    private double longitude;

    private String address;

    private String phoneNumber;

    private double rating;

    private String category;

    private String subDescription;

    private String description;

    private int reviewCount;

    private int bookmarkCount;

    public static RestaurantResponse from(Restaurant restaurant) {
        return RestaurantResponse.builder()
                .id(restaurant.getId())
                .name(restaurant.getName())
                .doorType(restaurant.getDoorType().getValue())
                .latitude(restaurant.getLatitude())
                .longitude(restaurant.getLongitude())
                .address(restaurant.getAddress())
                .phoneNumber(restaurant.getPhoneNumber())
                .rating(restaurant.getRating())
                .category(restaurant.getCategory().getValue())
                .subDescription(restaurant.getSubDescription().getValue())
                .description(restaurant.getDescription())
                .reviewCount(restaurant.getReviewCount())
                .bookmarkCount(restaurant.getBookmarkCount())
                .build();
    }
}
