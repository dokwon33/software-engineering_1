package UOSense.UOSense_Backend.dto;

import UOSense.UOSense_Backend.common.enumClass.Detail;
import UOSense.UOSense_Backend.entity.Report;
import UOSense.UOSense_Backend.entity.Review;
import UOSense.UOSense_Backend.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ReportRequest {
    private int reviewId;
    private Detail detail;
    private LocalDateTime createdAt;

    public static Report toEntity(ReportRequest reportRequest, Review review, User user) {
        return Report.builder()
                .review(review)
                .user(user)
                .detail(reportRequest.detail)
                .createdAt(reportRequest.createdAt)
                .build();
    }
}
