package UOSense.UOSense_Backend.dto;

import UOSense.UOSense_Backend.entity.Report;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ReportResponse {

    private int id;
    private int reviewId;
    private int userId;
    private String detail;
    private LocalDateTime createdAt;

    public static ReportResponse from(Report report) {
        return builder()
                .id(report.getId())
                .reviewId(report.getReview().getId())
                .userId(report.getUser().getId())
                .detail(report.getDetail().getValue())
                .createdAt(report.getCreatedAt())
                .build();
    }
}
