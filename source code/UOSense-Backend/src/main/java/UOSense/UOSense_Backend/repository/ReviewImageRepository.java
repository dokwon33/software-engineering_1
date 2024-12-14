package UOSense.UOSense_Backend.repository;

import UOSense.UOSense_Backend.entity.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewImageRepository extends JpaRepository<ReviewImage, Integer> {
    List<ReviewImage> findAllByReviewId(int reviewId);
    void deleteAllByReviewId(int reviewId);
}
