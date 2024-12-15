package UOSense.UOSense_Backend.service;

import UOSense.UOSense_Backend.common.Utils.ImageUtils;
import UOSense.UOSense_Backend.entity.Review;
import UOSense.UOSense_Backend.entity.ReviewImage;
import UOSense.UOSense_Backend.repository.ReviewImageRepository;
import UOSense.UOSense_Backend.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewImageServiceImpl implements ReviewImageService{
    private final ReviewImageRepository reviewImageRepository;
    private final ReviewRepository reviewRepository;
    private final ImageUtils imageUtils;
    final String S3_FOLDER_NAME = "review";

    @Override
    @Transactional
    public void register(int reviewId, List<MultipartFile> images) {
        Optional<Review> review = reviewRepository.findById(reviewId);
        if (review.isEmpty()) {
            throw new IllegalArgumentException("리뷰 정보가 없습니다.");
        }
        for (MultipartFile image : images) {
            if (image.isEmpty()) {
                throw new IllegalArgumentException("이미지 데이터가 없습니다.");
            }
            /* 1. 이미지 스토리지에 저장 */
            String path = imageUtils.uploadImageToS3(image, S3_FOLDER_NAME);
            /* 2. 엔티티 준비 */
            ReviewImage savingImg = ReviewImage.builder().review(review.get()).imageUrl(path).build();
            /* 3. DB에 저장 */
            reviewImageRepository.save(savingImg);
        }
    }

    @Override
    public List<String> find(int reviewId) {
        List<ReviewImage> reviewImages = reviewImageRepository.findAllByReviewId(reviewId);
        return reviewImages.stream()
                .map(ReviewImage::getImageUrl)
                .toList();
    }
}
