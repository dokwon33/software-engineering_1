package UOSense.UOSense_Backend.dto;

import UOSense.UOSense_Backend.common.enumClass.SubDescription;
import UOSense.UOSense_Backend.entity.PurposeRestaurant;
import UOSense.UOSense_Backend.entity.Restaurant;
import UOSense.UOSense_Backend.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class PurposeRestRequest {
    private int restaurantId;

    private String name;

    private String address;

    private String phoneNumber;

    private SubDescription subDescription;

    public static PurposeRestaurant toEntity(PurposeRestRequest purposeRestRequest, Restaurant restaurant, User user) {
        return PurposeRestaurant.builder()
                .restaurant(restaurant)
                .name(purposeRestRequest.getName())
                .address(purposeRestRequest.getAddress())
                .phoneNumber(purposeRestRequest.getPhoneNumber())
                .subDescription(purposeRestRequest.getSubDescription())
                .user(user)
                .build();
    }
}
