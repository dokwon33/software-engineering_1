package UOSense.UOSense_Backend.repository;

import UOSense.UOSense_Backend.common.enumClass.Category;
import UOSense.UOSense_Backend.common.enumClass.DoorType;
import UOSense.UOSense_Backend.common.enumClass.SubDescription;
import UOSense.UOSense_Backend.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Integer>{
    List<Restaurant> findByDoorTypeAndCategory(DoorType doorType, Category category);
    List<Restaurant> findByCategory(Category category);
    List<Restaurant> findByDoorType(DoorType doorType);
    List<Restaurant> findBySubDescription(SubDescription subDescription);
    @Query(value = "SELECT r.* " +
            "FROM Restaurant r " +
            "JOIN Avg_Price_View v ON r.id = v.restaurant_id " +
            "WHERE r.id IN :restaurantIds " +
            "ORDER BY v.avg_price ASC", nativeQuery = true)
    List<Restaurant> sortRestaurantsByAvgPrice(@Param("restaurantIds") List<Integer> restaurantIds);

    @Query("SELECT r FROM Restaurant r WHERE r.name LIKE %:keyword%")
    List<Restaurant> findByNameContains(@Param("keyword") String keyword);
}
