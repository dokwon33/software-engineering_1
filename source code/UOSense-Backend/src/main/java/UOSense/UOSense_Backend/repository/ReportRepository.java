package UOSense.UOSense_Backend.repository;

import UOSense.UOSense_Backend.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository extends JpaRepository<Report, Integer> {
    void deleteAllByReviewId(int reviewId);
}
