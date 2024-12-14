package UOSense.UOSense_Backend.service;

import UOSense.UOSense_Backend.common.Utils.ImageUtils;
import UOSense.UOSense_Backend.dto.*;
import UOSense.UOSense_Backend.entity.*;
import UOSense.UOSense_Backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PurposeServiceImpl implements PurposeService {
    private final PurposeRestaurantRepository purposeRestaurantRepository;
    private final PurposeRestImgRepository purposeRestImgRepository;
    private final ImageUtils imageUtils;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;

    @Override
    @Transactional
    public void delete(int purposeRestId) {
        if (!purposeRestaurantRepository.existsById(purposeRestId)) {
            throw new IllegalArgumentException("존재하지 않는 식당 제안 정보입니다");
        }
        List<String> restaurantImages = purposeRestImgRepository.findImageUrls(purposeRestId);
        if (!restaurantImages.isEmpty()) {
            restaurantImages.forEach(imageUtils::deleteImageInS3);
        }
        purposeRestaurantRepository.deleteById(purposeRestId);
    }

    @Override
    public List<PurposeRestResponse> findList(int restaurantId) {
       List<PurposeRestaurant> purposeRestaurant = purposeRestaurantRepository.findAllByRestaurantId(restaurantId);
       if (purposeRestaurant.isEmpty()) {
           throw new IllegalArgumentException("해당 식당 제안이 존재하지 않습니다.");
       }
       return purposeRestaurant.stream()
               .map(PurposeRestResponse::from)
               .toList();
    }
    @Override
    @Transactional
    public void register(PurposeRestRequest request, int userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new IllegalArgumentException("사용자 정보가 없습니다.");
        }
        Optional<Restaurant> restaurant = restaurantRepository.findById(request.getRestaurantId());
        if (restaurant.isEmpty()) {
            throw new IllegalArgumentException("식당 정보가 없습니다.");
        }
        PurposeRestaurant purposeRestaurant = PurposeRestRequest.toEntity(request, restaurant.get(), user.get());
        purposeRestaurantRepository.save(purposeRestaurant);
    }
  
    @Override
    public PurposeRestaurant getPurposeRestById(int purposeRestId) {
        return purposeRestaurantRepository.findById(purposeRestId)
                .orElseThrow(() -> new IllegalArgumentException("정보 수정 제안 이력이 존재하지 않습니다."));
    }
}
