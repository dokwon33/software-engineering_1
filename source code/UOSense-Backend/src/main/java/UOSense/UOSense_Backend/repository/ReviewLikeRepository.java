package UOSense.UOSense_Backend.repository;

import UOSense.UOSense_Backend.entity.ReviewLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {
    boolean existsByUserIdAndReviewId(int userId, int reviewId);
    void deleteAllByReviewId(int reviewId);
}