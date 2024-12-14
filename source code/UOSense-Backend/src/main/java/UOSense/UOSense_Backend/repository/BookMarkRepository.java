package UOSense.UOSense_Backend.repository;

import UOSense.UOSense_Backend.entity.BookMark;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookMarkRepository extends JpaRepository<BookMark, Integer> {
    List<BookMark> findAllByUserId(int userId);
    boolean existsByUserIdAndRestaurantId(int userId, int restaurantId);
    void deleteByUserIdAndRestaurantId(int userId, int restaurantId);
    void deleteAllByRestaurantId(int restaurantId);
}
