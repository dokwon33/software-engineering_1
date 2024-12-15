package UOSense.UOSense_Backend.service;

import UOSense.UOSense_Backend.common.Utils.ImageUtils;
import UOSense.UOSense_Backend.dto.MenuResponse;
import UOSense.UOSense_Backend.dto.PurposeMenuRequest;
import UOSense.UOSense_Backend.dto.PurposeMenuResponse;
import UOSense.UOSense_Backend.entity.Menu;
import UOSense.UOSense_Backend.entity.PurposeMenu;
import UOSense.UOSense_Backend.entity.Restaurant;
import UOSense.UOSense_Backend.entity.User;
import UOSense.UOSense_Backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.NoSuchElementException;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
@Transactional
public class PurposeMenuServiceImpl implements PurposeMenuService {
    private final PurposeMenuRepository purposeMenuRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;

    private final ImageUtils imageUtils;
    final String S3_FOLDER_NAME = "menu";

    @Override
    public List<PurposeMenuResponse> findListByRestaurantId(int restaurantId) {
        List<PurposeMenu> menuBoard = purposeMenuRepository.findAllByRestaurantId((restaurantId))
                .orElseThrow(()-> new NoSuchElementException("해당 식당에 메뉴 정보 수정 제안이 존재하지 않습니다."));

        return menuBoard.stream()
                .map(PurposeMenuResponse::from)
                .collect(toList());
    }

    @Override
    public void register(PurposeMenuRequest request, MultipartFile menuImage) {
        Restaurant purposeRest = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 식당입니다."));
        User proposer = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보가 없습니다."));
        String imageUrl = saveImage(menuImage);

        purposeMenuRepository.save(request.toEntity(purposeRest, proposer, imageUrl));
    }

    @Override
    public String saveImage(MultipartFile menuImage) {
        if (menuImage==null)
            return "";
        try {
            return imageUtils.uploadImageToS3(menuImage, S3_FOLDER_NAME);
        } catch ( RuntimeException e ) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    @Override
    public void delete(int menuId) {
        PurposeMenu menu = purposeMenuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 메뉴 정보 수정 제안이 존재하지 않습니다."));
        String oldImageUrl = menu.getImageUrl();
        if (oldImageUrl != null) {
            imageUtils.deleteImageInS3(oldImageUrl);
        }
        purposeMenuRepository.deleteById(menuId);
    }
}
