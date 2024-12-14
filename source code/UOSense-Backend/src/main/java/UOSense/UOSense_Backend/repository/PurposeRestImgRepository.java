package UOSense.UOSense_Backend.repository;

import UOSense.UOSense_Backend.entity.PurposeRestImg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurposeRestImgRepository extends JpaRepository<PurposeRestImg, Integer>{
    @Query(value = "SELECT ri.image_url " +
            "FROM Purpose_Restaurant_Image ri " +
            "WHERE ri.restaurant_id = :restaurantId " , nativeQuery = true)
    List<String> findImageUrls(@Param("restaurantId") int restaurantId);
    
    @Query(value = "SELECT p.* " +
            "FROM Purpose_Restaurant_Image p " +
            "WHERE p.restaurant_id = :restaurantId", nativeQuery = true)
    List<PurposeRestImg> findAllByRestaurantId(@Param("restaurantId") int restaurantId);
}
