package UOSense.UOSense_Backend.dto;

import UOSense.UOSense_Backend.entity.PurposeRestaurant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class PurposeRestResponse {
    private int id;

    private int restaurantId;

    private String name;

    private String address;

    private String phoneNumber;

    private String subDescription;

    public static PurposeRestResponse from(PurposeRestaurant purposeRestaurant) {
        return PurposeRestResponse.builder()
                .id(purposeRestaurant.getId())
                .restaurantId(purposeRestaurant.getRestaurant().getId())
                .name(purposeRestaurant.getName())
                .address(purposeRestaurant.getAddress())
                .phoneNumber(purposeRestaurant.getPhoneNumber())
                .subDescription(purposeRestaurant.getSubDescription().getValue())
                .build();
    }
}
