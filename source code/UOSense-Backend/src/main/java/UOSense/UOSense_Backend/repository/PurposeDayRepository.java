package UOSense.UOSense_Backend.repository;

import UOSense.UOSense_Backend.entity.PurposeBusinessDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurposeDayRepository extends JpaRepository<PurposeBusinessDay, Integer> {
    List<PurposeBusinessDay> findAllByRestaurantId(int restaurantId);
}