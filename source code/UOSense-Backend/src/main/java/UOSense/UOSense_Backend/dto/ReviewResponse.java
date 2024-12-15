package UOSense.UOSense_Backend.dto;

import UOSense.UOSense_Backend.entity.Review;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ReviewResponse {
    private int id;

    private int restaurantId;

    private int userId;

    private String nickname;

    private String userImage;

    private String body;

    private double rating;

    private LocalDateTime dateTime;

    private boolean reviewEventCheck;

    private String tag;

    private int likeCount;

    private List<String> imageUrls;

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public static ReviewResponse from(Review review){
        return ReviewResponse.builder()
                .id(review.getId())
                .restaurantId(review.getRestaurant().getId())
                .userId(review.getUser().getId())
                .nickname(review.getUser().getNickname())
                .userImage(review.getUser().getImageUrl())
                .body(review.getBody())
                .rating(review.getRating())
                .dateTime(review.getDateTime())
                .reviewEventCheck(review.isReviewEventCheck())
                .tag(review.getTag().getValue())
                .likeCount(review.getLikeCount())
                .build();
    }
}
