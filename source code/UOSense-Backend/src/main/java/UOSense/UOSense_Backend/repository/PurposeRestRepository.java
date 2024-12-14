package UOSense.UOSense_Backend.repository;

import UOSense.UOSense_Backend.entity.PurposeRestaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PurposeRestRepository extends JpaRepository<PurposeRestaurant, Integer> {
}
