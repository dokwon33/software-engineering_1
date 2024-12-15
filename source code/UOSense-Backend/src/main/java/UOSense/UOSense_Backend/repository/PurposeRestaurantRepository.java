package UOSense.UOSense_Backend.repository;

import UOSense.UOSense_Backend.entity.PurposeRestaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurposeRestaurantRepository extends JpaRepository<PurposeRestaurant, Integer>{
    List<PurposeRestaurant> findAllByRestaurantId(int restaurantId);
}
