package UOSense.UOSense_Backend.dto;

import UOSense.UOSense_Backend.common.enumClass.Tag;
import UOSense.UOSense_Backend.entity.Restaurant;
import UOSense.UOSense_Backend.entity.Review;
import UOSense.UOSense_Backend.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ReviewRequest {
    private int restaurantId;

    private String body;

    private double rating;

    private LocalDateTime dateTime;

    private boolean reviewEventCheck;

    private Tag tag;

    public static Review toEntity(ReviewRequest reviewRequest, User user, Restaurant restaurant) {
        return Review.builder()
                .user(user)
                .restaurant(restaurant)
                .body(reviewRequest.getBody())
                .rating(reviewRequest.getRating())
                .dateTime(reviewRequest.getDateTime())
                .reviewEventCheck(reviewRequest.isReviewEventCheck())
                .tag(reviewRequest.getTag())
                .likeCount(0)
                .build();
    }
}
