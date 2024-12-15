package UOSense.UOSense_Backend.service;

import UOSense.UOSense_Backend.common.enumClass.DoorType;
import UOSense.UOSense_Backend.dto.RestaurantListResponse;
import UOSense.UOSense_Backend.entity.Restaurant;

import java.util.List;

public interface SearchService {
    enum sortFilter { DEFAULT, BOOKMARK, DISTANCE, RATING, REVIEW, PRICE }
    /**
     * 입력어와 출입구문 필터를 바탕으로 식당을 검색합니다.
     */
    List<Restaurant> findByKeyword(String keyword);
    /**
     * 출입구문으로 필터링합니다.
     */
    List<Restaurant> filterByDoorType(List<Restaurant> result, DoorType doorType);
    List<RestaurantListResponse> sort(List<Restaurant> result, sortFilter filter);
    /**
     * 캐시에서 해당 키워드로 검색된 식당목록을 가져옵니다.
     */
    List<Restaurant> checkRestaurantCache(String keyword);
    boolean isInCache(String keyword);
}
