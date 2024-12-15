package UOSense.UOSense_Backend.service;

import UOSense.UOSense_Backend.dto.ReportRequest;
import UOSense.UOSense_Backend.dto.ReportResponse;
import UOSense.UOSense_Backend.entity.Report;
import UOSense.UOSense_Backend.entity.Review;
import UOSense.UOSense_Backend.entity.User;
import UOSense.UOSense_Backend.repository.ReportRepository;
import UOSense.UOSense_Backend.repository.ReviewRepository;
import UOSense.UOSense_Backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService{
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;

    @Override
    @Transactional
    public void register(ReportRequest reportRequest, int userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new IllegalArgumentException("사용자 정보가 없습니다.");
        }
        Optional<Review> review = reviewRepository.findById(reportRequest.getReviewId());
        if (review.isEmpty()) {
            throw new IllegalArgumentException("리뷰 정보가 없습니다.");
        }

        Report report = ReportRequest.toEntity(reportRequest, review.get(), user.get());
        reportRepository.save(report);
    }

    @Override
    public List<ReportResponse> findList() {
        List<Report> reports = reportRepository.findAll();
        return reports.stream()
                .map(ReportResponse::from)
                .toList();
    }
}
