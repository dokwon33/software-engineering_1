package UOSense.UOSense_Backend.service;

import UOSense.UOSense_Backend.common.converter.CategoryConverter;
import UOSense.UOSense_Backend.common.converter.EnumBaseConverter;
import UOSense.UOSense_Backend.common.converter.SubDescriptionConverter;
import UOSense.UOSense_Backend.common.enumClass.Category;
import UOSense.UOSense_Backend.common.enumClass.DoorType;
import UOSense.UOSense_Backend.common.enumClass.SubDescription;
import UOSense.UOSense_Backend.dto.RestaurantListResponse;
import UOSense.UOSense_Backend.entity.Restaurant;
import UOSense.UOSense_Backend.entity.RestaurantImage;
import UOSense.UOSense_Backend.repository.MenuRepository;
import UOSense.UOSense_Backend.repository.RestaurantImageRepository;
import UOSense.UOSense_Backend.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
@CacheConfig(cacheNames = "restaurantCache")
@Service
@RequiredArgsConstructor
@Transactional
public class SearchServiceImpl implements SearchService{
    private final RestaurantRepository restaurantRepository;
    private final RestaurantImageRepository restaurantImageRepository;
    private final MenuRepository menuRepository;
    private final CacheManager cacheManager;

    @Cacheable(value = "restaurantCache", key = "#keyword")
    @Override
    public List<Restaurant> findByKeyword(String keyword) {
        List<Restaurant> result;
        // 1. 세부 분류 (e.g. 술집, 카페, 음식점) 내 검색
        if (Arrays.stream(SubDescription.values()).anyMatch(subDescription -> subDescription.getValue().equals(keyword))) {
            EnumBaseConverter<SubDescription> converter = new SubDescriptionConverter();
            SubDescription description = converter.convertToEntityAttribute(keyword);
            result = restaurantRepository.findBySubDescription(description);
        // 2. 음식 종류 (e.g. 한식, 중식) 내 검색
        } else if (Arrays.stream(Category.values()).anyMatch(category -> category.getValue().equals(keyword))) {
            EnumBaseConverter<Category> converter = new CategoryConverter();
            Category category = converter.convertToEntityAttribute(keyword);
            result = restaurantRepository.findByCategory(category);

        } else {    // 3. 메뉴명, 식당이름
            List<Restaurant> restaurants = restaurantRepository.findByNameContains(keyword);
            List<Restaurant> menus = menuRepository.findByNameContains(keyword);
          
            if (restaurants.isEmpty() && menus.isEmpty()) {
                throw new NoSuchElementException("검색할 정보가 존재하지 않습니다.");
            }

            // 중복을 제거하기 위해 Set 사용
            Set<Restaurant> uniqueSet = new HashSet<>();
            uniqueSet.addAll(restaurants);
            uniqueSet.addAll(menus);

            result = new ArrayList<>(uniqueSet);
        }

        if (result == null) {
            throw new NoSuchElementException("결과가 존재하지 않습니다.");
        }
        return result;
    }

    @Override
    public List<Restaurant> filterByDoorType(List<Restaurant> result, DoorType doorType) {
        return result.stream()
                .filter(restaurant -> restaurant.getDoorType().equals(doorType))
                .collect(Collectors.toList());
    }

    @Override
    public List<RestaurantListResponse> sort(List<Restaurant> result, sortFilter filter) {
        return convertToListDTO(switch (filter) {
            case DEFAULT, BOOKMARK -> // 선호도 기준: 즐겨찾기 많은 순
                    result.stream()
                            .sorted(Comparator.comparing(Restaurant::getBookmarkCount).reversed())
                            .toList();
            case DISTANCE ->// 거리 기준: 가까운 순 (원본 그대로 보내고 프론트에서 처리)
                    result;
            case RATING ->  // 평점 기준: 리뷰 평점이 높은 순
                    result.stream()
                            .sorted(Comparator.comparing(Restaurant::getRating).reversed())
                            .toList();
            case REVIEW ->  // 리뷰 수 기준: 리뷰 많은 순
                    result.stream()
                            .sorted(Comparator.comparing(Restaurant::getReviewCount).reversed())
                            .toList();
            case PRICE ->   // 착한 가격 기준: 평균 가격 낮은 순
                    restaurantRepository.sortRestaurantsByAvgPrice(result.stream()
                            .map(Restaurant::getId)
                            .collect(Collectors.toList()));
        });
    }

    private List<RestaurantListResponse> convertToListDTO (List<Restaurant> sortedList) {
        List<Integer> sortedRestaurantIds = sortedList.stream().map(Restaurant::getId).toList();
        // restaurantId -> imageUrl 매핑 생성
        Map<Integer, String> imageList = restaurantImageRepository.findAllFirstImageUrl(sortedRestaurantIds)
                .stream().collect(Collectors.toMap(
                        image -> image.getRestaurant().getId(), // Key: restaurantId
                        RestaurantImage::getImageUrl            // Value: imageUrl
                ));
        // DTO 변환
        return sortedList.stream()
                .map(restaurant -> {
                    String imageUrl = imageList.getOrDefault(restaurant.getId(), null);
                    return RestaurantListResponse.from(restaurant, imageUrl);
                })
                .toList();
    }
    @Override
    public List<Restaurant> checkRestaurantCache(String keyword) {
        // CacheManager를 통해 Caffeine Cache 가져오기
        org.springframework.cache.Cache cache = cacheManager.getCache("restaurantCache");
        if (cache == null) {
            throw new IllegalStateException("restaurantCache가 설정되지 않았습니다.");
        }

        // 캐시에서 데이터 조회
        List<Restaurant> cachedResults = cache.get(keyword, List.class);
        if (cachedResults == null) {
            throw new NoSuchElementException("캐시된 데이터가 없습니다.");
        }
        return cachedResults;
    }

    @Override
    public boolean isInCache(String keyword) {
        org.springframework.cache.Cache cache = cacheManager.getCache("restaurantCache");
        if (cache == null) {
            return false;
        }

        List<Restaurant> cachedResults = cache.get(keyword, List.class);
        if (cachedResults == null) {
            return false;
        }
        return !cachedResults.isEmpty();
    }
}
