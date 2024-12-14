package UOSense.UOSense_Backend.common.Utils;

import UOSense.UOSense_Backend.dto.RestaurantListResponse;
import UOSense.UOSense_Backend.dto.SearchPair;
import UOSense.UOSense_Backend.entity.Restaurant;
import UOSense.UOSense_Backend.entity.RestaurantImage;
import UOSense.UOSense_Backend.repository.RestaurantImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class SearchUtils {
    private static final int MAX_SIZE = 20;
    private static final int MAX_DIS = 20; // 최대 레벤슈타인 거리. 직접 테스트하면서 수정해야 할 거 같아요
    private static RestaurantImageRepository restaurantImageRepository;

    @Autowired
    public void setRestaurantImageRepository(RestaurantImageRepository restaurantImageRepository) {
        SearchUtils.restaurantImageRepository = restaurantImageRepository;
    }

    /**
     *
     * 두 문자열 사이의 거리를 측정하는 알고리즘
     */
    public static int damerauLevenshteinDistance(String s1, String s2) {
        int m = s1.length();
        int n = s2.length();

        // dp 테이블 초기화
        int[][] dp = new int[m + 1][n + 1];

        // dp[0][0]은 0으로 초기화 (빈 문자열 간의 거리)
        for (int i = 0; i <= m; i++) {
            dp[i][0] = i;  // 첫 번째 문자열과 빈 문자열 간의 거리 (삭제)
        }

        for (int j = 0; j <= n; j++) {
            dp[0][j] = j;  // 두 번째 문자열과 빈 문자열 간의 거리 (삽입)
        }

        // dp 테이블 채우기
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                // 교체 비용 계산 (s1[i-1]과 s2[j-1]가 같으면 0, 다르면 1)
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;

                // 삽입, 삭제, 교체 중 최소 값 선택
                dp[i][j] = Math.min(dp[i - 1][j] + 1, // 삭제
                        Math.min(dp[i][j - 1] + 1,       // 삽입
                                dp[i - 1][j - 1] + cost));       // 교체

                // 문자 교환 (Transposition)
                if (i > 1 && j > 1 && s1.charAt(i - 1) == s2.charAt(j - 2) && s1.charAt(i - 2) == s2.charAt(j - 1)) {
                    dp[i][j] = Math.min(dp[i][j], dp[i - 2][j - 2] + 1); // 교환 비용 1 추가
                }
            }
        }

        // 최종 거리 반환
        return dp[m][n];
    }

    /**
     *
     * 리스트를 거리가 짧은 순으로 정렬해 중복을 허용하지 않고 20(MAX_SIZE)개의 결과를 선택.
     */
    public static List<Restaurant> select(List<SearchPair> inputList) {
        // 오름차순으로 정렬
        Collections.sort(inputList);
        // 결과 리스트
        List<Restaurant> resultList = new ArrayList<>();
        // 확정된 이름을 저장하는 set
        Set<String> selectedNames = new HashSet<>();

        for (SearchPair input : inputList) {
            if (!selectedNames.contains(input.getRestaurant().getName()) && input.getDistance() <= MAX_DIS) {
                resultList.add(input.getRestaurant());
                selectedNames.add(input.getRestaurant().getName());
            }
            if (resultList.size() == MAX_SIZE) {
                // 최대 개수 도달 시 break;
                break;
            }
        }

        return resultList;
    }

    public static List<RestaurantListResponse> convertToListDTO (List<Restaurant> sortedList) {
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
}
