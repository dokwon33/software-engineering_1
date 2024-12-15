package UOSense.UOSense_Backend.repository;

import UOSense.UOSense_Backend.entity.BusinessDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BusinessDayRepository extends JpaRepository<BusinessDay, Integer> {
    List<BusinessDay> findAllByRestaurantId(int restaurantId);
    void deleteAllByRestaurantId(int restaurantId);
}