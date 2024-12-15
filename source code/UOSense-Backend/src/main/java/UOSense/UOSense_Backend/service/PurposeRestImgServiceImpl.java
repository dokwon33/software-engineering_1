package UOSense.UOSense_Backend.service;

import UOSense.UOSense_Backend.common.Utils.ImageUtils;
import UOSense.UOSense_Backend.dto.ImageInfo;
import UOSense.UOSense_Backend.dto.RestaurantImagesResponse;
import UOSense.UOSense_Backend.entity.PurposeRestImg;
import UOSense.UOSense_Backend.entity.PurposeRestaurant;
import UOSense.UOSense_Backend.repository.PurposeRestImgRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional
public class PurposeRestImgServiceImpl implements PurposeRestImgService {
    private final PurposeRestImgRepository purposeRestImgRepository;
    private final ImageUtils imageUtils;
    final String S3_FOLDER_NAME = "purpose";
    @Override
    public RestaurantImagesResponse showImageList(int restaurantId) {
        List<PurposeRestImg> restaurantImages = purposeRestImgRepository.findAllByRestaurantId(restaurantId);
        if (restaurantImages.isEmpty()) throw new NoSuchElementException("해당 식당 정보 수정 제안에 사진이 존재하지 않습니다.");

        List<ImageInfo> imageList = restaurantImages.stream()
                .map(this::getMetaData)
                .toList();
        return new RestaurantImagesResponse(restaurantId, imageList);
    }

    @Override
    public RestaurantImagesResponse save(PurposeRestaurant restaurant, int userId, MultipartFile[] images) {
        List<ImageInfo> imageList = new ArrayList<>();
        if (userId != restaurant.getUser().getId()) {
            throw new IllegalArgumentException("해당 식당 정보 수정 제안 등록자 사진 등록자가 일치하지 않습니다.");
        }

        for (MultipartFile image : images) {
            /* 1. 이미지 스토리지에 저장 */
            String path = imageUtils.uploadImageToS3(image, S3_FOLDER_NAME);
            /* 2. 엔티티 준비 */
            PurposeRestImg savingImg = PurposeRestImg.builder()
                    .user(restaurant.getUser())
                    .purposeRestaurant(restaurant)
                    .imageUrl(path)
                    .build();
            /* 3. DB에 저장 */
            PurposeRestImg savedImg = purposeRestImgRepository.save(savingImg);
            /* 4. 응답 데이터 준비 */
            imageList.add(getMetaData(savedImg));
        }
        return new RestaurantImagesResponse(restaurant.getId(), imageList);
    }

    @Override
    public void delete(int id) {
        PurposeRestImg restaurantImage = purposeRestImgRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 사진이 존재하지 않습니다."));

        imageUtils.deleteImageInS3(restaurantImage.getImageUrl());
        purposeRestImgRepository.deleteById(restaurantImage.getId());
    }

    public ImageInfo getMetaData(PurposeRestImg image) {
        return new ImageInfo(image.getId(),image.getImageUrl());
    }
}
