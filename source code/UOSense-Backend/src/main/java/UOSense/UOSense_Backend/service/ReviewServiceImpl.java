package UOSense.UOSense_Backend.service;

import UOSense.UOSense_Backend.common.Utils.ImageUtils;
import UOSense.UOSense_Backend.dto.ReviewRequest;
import UOSense.UOSense_Backend.dto.ReviewResponse;
import UOSense.UOSense_Backend.entity.*;
import UOSense.UOSense_Backend.repository.*;

import lombok.RequiredArgsConstructor;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewServiceImpl implements ReviewService{
    private final ReviewRepository reviewRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final ImageUtils imageUtils;
    private final ReportRepository reportRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> findListByRestaurantId(int restaurantId) {
        if (!restaurantRepository.existsById(restaurantId))
            throw new IllegalArgumentException("존재하지 않는 식당입니다.");

        List<Review> reviewList = reviewRepository.findAllByRestaurantId(restaurantId);
        return reviewList.stream()
                .map(ReviewResponse::from)
                .toList();
    }

    @Override
    public void delete(int reviewId) {
        if (!reviewRepository.existsById(reviewId)) {
            throw new IllegalArgumentException("삭제할 리뷰가 존재하지 않습니다.");
        }
        List<ReviewImage> reviewImages = reviewImageRepository.findAllByReviewId(reviewId);
        if (!reviewImages.isEmpty()) {
            for (ReviewImage reviewImage : reviewImages) {
                imageUtils.deleteImageInS3(reviewImage.getImageUrl());
            }
        }
        reviewImageRepository.deleteAllByReviewId(reviewId);
        reviewLikeRepository.deleteAllByReviewId(reviewId);
        reportRepository.deleteAllByReviewId(reviewId);
        reviewRepository.deleteById(reviewId);
    }

    @Override
    public void deleteByRestaurantId(int restaurantId) {
        List<Review> reviewList = reviewRepository.findAllByRestaurantId(restaurantId);
        if (reviewList.isEmpty()) {
            return;
        }
        reviewList.forEach(review -> delete(review.getId()));
    }

    @Override
    public void addLike(int userId,int reviewId) {
        try {
            // 1. 좋아요 이력 저장 시도
            reviewLikeRepository.save(new ReviewLike(
                    userRepository.getReferenceById(userId),    // 프록시 객체
                    reviewRepository.getReferenceById(reviewId) // 프록시 객체
            ));
        } catch (EntityNotFoundException e) {
            // 사용자가 존재하지 않는 경우
            throw new NoSuchElementException("사용자 혹은 리뷰를 찾을 수 없습니다.");
        } catch (DataIntegrityViolationException e) {
            // 이미 좋아요를 누른 경우 (unique constraint 위반)
            throw new IllegalStateException("이미 좋아요를 누른 리뷰입니다.");
        }

        // 2. 리뷰 좋아요 개수 증가
        int updatedRows = reviewRepository.increaseLikeCount(reviewId);
        if (updatedRows == 0) {
            throw new NoSuchElementException("해당 리뷰가 존재하지 않습니다.");
        }
    }
    
    @Override
    public int register(ReviewRequest reviewRequest, int userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new IllegalArgumentException("사용자 정보가 없습니다.");
        }
        Optional<Restaurant> restaurant = restaurantRepository.findById(reviewRequest.getRestaurantId());
        if (restaurant.isEmpty()) {
            throw new IllegalArgumentException("식당 정보가 없습니다.");
        }

        Review review = ReviewRequest.toEntity(reviewRequest, user.get(), restaurant.get());

        review = reviewRepository.save(review);
        return review.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewResponse find(int reviewId) {
        Optional<Review> review = reviewRepository.findById(reviewId);
        if (review.isEmpty()) {
            throw new IllegalArgumentException("리뷰 정보가 없습니다.");
        }
        return ReviewResponse.from(review.get());
    }

    // restaurantId로 리뷰 목록 조회와 헷갈릴 우려가 있어 ByUserId를 추가함
    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> findByUserId(int userId) {
        List<Review> reviewList = reviewRepository.findAllByUserId(userId);
        if (reviewList.isEmpty()) {
            throw new IllegalArgumentException("해당 사용자의 리뷰 정보가 없습니다.");
        }
        return reviewList.stream()
                .map(ReviewResponse::from)
                .toList();
    }
}
