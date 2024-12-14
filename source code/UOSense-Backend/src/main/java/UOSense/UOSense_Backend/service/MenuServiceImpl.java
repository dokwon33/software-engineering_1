package UOSense.UOSense_Backend.service;

import UOSense.UOSense_Backend.common.Utils.ImageUtils;
import UOSense.UOSense_Backend.dto.MenuRequest;
import UOSense.UOSense_Backend.entity.Menu;
import UOSense.UOSense_Backend.entity.Restaurant;
import UOSense.UOSense_Backend.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuServiceImpl implements MenuService{
    private final MenuRepository menuRepository;
    private final ImageUtils imageUtils;
    final String S3_FOLDER_NAME = "menu";

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

    @Transactional
    @Override
    public void edit(MenuRequest menuRequest, MultipartFile image, Restaurant restaurant) {
        Menu menu = menuRepository.findById(menuRequest.getId())
                .orElseThrow(() -> new IllegalArgumentException("수정할 메뉴가 존재하지 않습니다."));
        String oldImageUrl = menu.getImageUrl();
        String newImageUrl = "";
        if (image != null) {  // 수정할 사진이 있음
            // 기존 사진이 존재함
            if (oldImageUrl != null) {
                imageUtils.deleteImageInS3(oldImageUrl);
            }
            newImageUrl = imageUtils.uploadImageToS3(image,S3_FOLDER_NAME);
        }

        menuRepository.save(menuRequest.toEntity(restaurant, newImageUrl));
    }

    @Override
    @Transactional
    public void delete(int menuId) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 메뉴가 존재하지 않습니다."));
        String oldImageUrl = menu.getImageUrl();
        if (oldImageUrl != null) {
            imageUtils.deleteImageInS3(oldImageUrl);
        }
        menuRepository.deleteById(menuId);
    }
}