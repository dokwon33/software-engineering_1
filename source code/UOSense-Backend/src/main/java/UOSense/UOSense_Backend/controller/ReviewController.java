package UOSense.UOSense_Backend.controller;

import UOSense.UOSense_Backend.dto.CustomUserDetails;
import UOSense.UOSense_Backend.dto.ReviewRequest;
import UOSense.UOSense_Backend.dto.ReviewResponse;
import UOSense.UOSense_Backend.service.ReviewImageService;
import UOSense.UOSense_Backend.service.ReviewService;
import UOSense.UOSense_Backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.NoSuchElementException;
import java.util.Collections;
import java.util.List;

@Tag(name = "리뷰 관리")
@RestController
@RequestMapping("/api/v1/review")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;
    private final ReviewImageService reviewImageService;
    private final UserService userService;

    @DeleteMapping("/delete")
    @Operation(summary = "리뷰 삭제", description = "리뷰를 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "리뷰를 성공적으로 삭제했습니다."),
            @ApiResponse(responseCode = "404", description = "삭제할 리뷰를 찾을 수 없습니다."),
            @ApiResponse(responseCode = "417", description = "AWS S3에서 사진 삭제에 실패했습니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류입니다.")
    })
    public ResponseEntity<Void> delete(@RequestParam int reviewId) {
        try {
            reviewService.delete(reviewId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (RuntimeException e) {
          return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).build();
        }
    }

    @PostMapping("/create")
    @Operation(summary = "리뷰 등록", description = "리뷰를 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "리뷰를 성공적으로 등록했습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류입니다.")
    })
    public ResponseEntity<Integer> create(@RequestBody ReviewRequest reviewRequest, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();

        try {
            int userId = userService.findId(email);
            int reviewId = reviewService.register(reviewRequest, userId);
            return new ResponseEntity<>(reviewId, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }


    @GetMapping("/get/list")
    @Operation(summary = "특정 식당 리뷰 일괄 조회", description = "해당 식당의 리뷰를 모두 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "리뷰를 모두 성공적으로 불러왔습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
            @ApiResponse(responseCode = "404", description = "조회할 식당을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류입니다.")
    })
    public ResponseEntity<List<ReviewResponse>> getListByRestaurantId(@RequestParam int restaurantId) {
        try {
            List<ReviewResponse> reviewList = reviewService.findListByRestaurantId(restaurantId);
            for (ReviewResponse reviewResponse : reviewList) {
                List<String> imageUrls = reviewImageService.find(reviewResponse.getId());
                reviewResponse.setImageUrls(imageUrls);
            }
            return new ResponseEntity<>(reviewList, HttpStatus.OK);
        } catch(IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/like")
    @Operation(summary = "리뷰 좋아요", description = "리뷰의 좋아요 수를 1 증가시킵니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "리뷰 추천 수가 1 증가하였습니다."),
            @ApiResponse(responseCode = "403", description = "좋아요는 한 번만 가능합니다."),
            @ApiResponse(responseCode = "404", description = "리뷰를 찾을 수 없습니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류입니다.")
    })
    public ResponseEntity<Void> addLike(@RequestParam int reviewId, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();

        try {
            int userId = userService.findId(email);
            reviewService.addLike(userId, reviewId);
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PostMapping(value ="/create/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "리뷰 이미지 등록", description = "리뷰 이미지를 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "리뷰 이미지를 성공적으로 등록했습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
            @ApiResponse(responseCode = "417", description = "AWS S3에서 사진 등록에 실패했습니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류입니다.")
    })
    public ResponseEntity<Void> createImages(@RequestPart List<MultipartFile> images,
                                             @RequestParam("reviewId") int reviewId) {
        try {
            reviewImageService.register(reviewId, images);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).build();
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/get/user")
    @Operation(summary = "특정 사용자 리뷰 조회", description = "특정 사용자의 리뷰 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 리뷰 목록을 조회했습니다."),
            @ApiResponse(responseCode = "404", description = "해당 사용자의 리뷰 정보가 없습니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류입니다.")
    })
    public ResponseEntity<List<ReviewResponse>> getByUserId(@RequestParam int userId) {
        try {
            List<ReviewResponse> reviewList = reviewService.findByUserId(userId);
            for (ReviewResponse reviewResponse : reviewList) {
                List<String> imageUrls = reviewImageService.find(reviewResponse.getId());
                reviewResponse.setImageUrls(imageUrls);
            }
            return new ResponseEntity<>(reviewList, HttpStatus.OK);
        } catch(IllegalArgumentException e) {
            return new ResponseEntity<>(Collections.emptyList(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/get/mine")
    @Operation(summary = "자신 리뷰 조회", description = "자신의 리뷰 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 리뷰 목록을 조회했습니다."),
            @ApiResponse(responseCode = "404", description = "자신의 리뷰 정보가 없습니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류입니다.")
    })
    public ResponseEntity<List<ReviewResponse>> getMine(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();

        try {
            int userId = userService.findId(email);
            List<ReviewResponse> reviewList = reviewService.findByUserId(userId);
            for (ReviewResponse reviewResponse : reviewList) {
                List<String> imageUrls = reviewImageService.find(reviewResponse.getId());
                reviewResponse.setImageUrls(imageUrls);
            }
            return new ResponseEntity<>(reviewList, HttpStatus.OK);
        } catch(IllegalArgumentException e) {
            return new ResponseEntity<>(Collections.emptyList(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/get")
    @Operation(summary = "단일 리뷰 조회", description = "단일 리뷰를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 리뷰를 조회했습니다."),
            @ApiResponse(responseCode = "400", description = "리뷰 정보가 없습니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류입니다.")
    })
    public ResponseEntity<ReviewResponse> get(@RequestParam int reviewId) {
        try {
            ReviewResponse reviewResponse = reviewService.find(reviewId);
            List<String> imageUrls = reviewImageService.find(reviewResponse.getId());
            reviewResponse.setImageUrls(imageUrls);
            return new ResponseEntity<>(reviewResponse, HttpStatus.OK);
        } catch(IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
