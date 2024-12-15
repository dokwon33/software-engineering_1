package UOSense.UOSense_Backend.service;

import UOSense.UOSense_Backend.dto.RestaurantImagesResponse;
import UOSense.UOSense_Backend.entity.PurposeRestaurant;
import UOSense.UOSense_Backend.entity.Restaurant;
import UOSense.UOSense_Backend.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.NoSuchElementException;

public interface PurposeRestImgService {
    /**
     * 해당 식당의 이미지 목록을 반환합니다.
     * @param restaurantId 식당 ID
     * @throws NoSuchElementException DB 조회 결과가 빈 경우
     */
    RestaurantImagesResponse showImageList(int restaurantId);
    /**
     * 이미지 원본 파일은 스토리지에 저장하고 DB에 저장된 URL목록을 반환합니다.
     * @throws IllegalArgumentException 저장할 엔티티가 빈 경우
     */
    RestaurantImagesResponse save(PurposeRestaurant restaurant, int userId, MultipartFile[] images);
    /**
     * 해당 식당 사진을 삭제합니다.
     */
    void delete(int id);
}
