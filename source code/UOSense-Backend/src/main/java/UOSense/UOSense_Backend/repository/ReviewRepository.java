package UOSense.UOSense_Backend.repository;

import UOSense.UOSense_Backend.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {
    List<Review> findAllByRestaurantId(int restaurantId);
    @Modifying
    @Query("UPDATE Review r SET r.likeCount = r.likeCount + 1 WHERE r.id = :reviewId")
    int increaseLikeCount(@Param("reviewId") int reviewId);
    List<Review> findAllByUserId(int userId);
}
