package UOSense.UOSense_Backend.controller;

import UOSense.UOSense_Backend.dto.*;
import UOSense.UOSense_Backend.entity.PurposeRestaurant;
import UOSense.UOSense_Backend.service.PurposeDayService;
import UOSense.UOSense_Backend.service.PurposeMenuService;
import UOSense.UOSense_Backend.service.PurposeRestImgServiceImpl;
import UOSense.UOSense_Backend.service.PurposeService;
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

import java.util.List;
import java.util.NoSuchElementException;

@Tag(name = "정보 수정 제안 관리")
@RestController
@RequestMapping("/api/v1/purpose")
@RequiredArgsConstructor
public class PurposeController {
    private final PurposeService purposeService;
    private final PurposeDayService purposeDayService;
    private final PurposeRestImgServiceImpl purposeRestImgService;
    private final PurposeMenuService purposeMenuService;
    private final UserService userService;

    @PostMapping("/create/restaurant")
    @Operation(summary = "식당 제안 생성", description = "식당 제안을 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "식당 제안을 생성했습니다."),
            @ApiResponse(responseCode = "400", description = "제안 정보가 올바르지 않습니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류입니다.")
    })
    public ResponseEntity<Void> createRestaurant(@RequestBody PurposeRestRequest request, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();

        try {
            int userId = userService.findId(email);
            purposeService.register(request, userId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/create/businessday")
    @Operation(summary = "영업 정보 제안 생성", description = "영업 정보 제안을 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "영업 정보 제안을 생성했습니다."),
            @ApiResponse(responseCode = "400", description = "제안 정보가 올바르지 않습니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류입니다.")
    })
    public ResponseEntity<Void> createPurposeDay(@RequestBody PurposeDayList purposeDayList, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();

        try {
            int userId = userService.findId(email);
            purposeDayService.register(purposeDayList, userId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get/restaurant")
    @Operation(summary = "정보 수정 제안 조회", description = "정보 수정 제안을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "정보 수정 제안을 성공적으로 조회했습니다."),
            @ApiResponse(responseCode = "404", description = "해당 정보 수정 제안이 존재하지 않습니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류입니다.")
    })
    public ResponseEntity<List<PurposeRestResponse>> getPurposeRestaurant(@RequestParam int restaurantId) {
        try {
            List<PurposeRestResponse> response = purposeService.findList(restaurantId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get/businessday")
    @Operation(summary = "영업 정보 제안 조회", description = "영업 정보 제안을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "영업 정보 제안을 성공적으로 조회했습니다."),
            @ApiResponse(responseCode = "404", description = "영업 정보 제안이 존재하지 않습니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류입니다.")
    })
    public ResponseEntity<PurposeDayList> getPurposeDay(@RequestParam int restaurantId) {
        try {
            PurposeDayList purposeDayList = purposeDayService.find(restaurantId);
            return new ResponseEntity<>(purposeDayList, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/restaurant")
    @Operation(summary = "식당 수정 제안 삭제", description = "식당 수정 제안을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "식당 수정 제안을 성공적으로 삭제했습니다."),
            @ApiResponse(responseCode = "404", description = "삭제할 식당 수정 제안을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류입니다.")
    })
    public ResponseEntity<Void> deletePurposeRestaurant(@RequestParam int purposeRestId){
        try {
            purposeService.delete(purposeRestId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete/businessday")
    @Operation(summary = "영업 정보 수정 제안 삭제", description = "영업 정보 수정 제안을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "영업 정보 수정 제안을 성공적으로 삭제했습니다."),
            @ApiResponse(responseCode = "404", description = "삭제할 영업 정보 수정 제안을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류입니다.")
    })
    public ResponseEntity<Void> deletePurposeDay(@RequestParam int purposeDayId) {
        try {
            purposeDayService.delete(purposeDayId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/get/images")
    @Operation(summary = "식당 정보 수정 제안 사진 조회", description = "사진을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사진을 성공적으로 불러왔습니다."),
            @ApiResponse(responseCode = "404", description = "사진을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "500", description = "잘못된 요청입니다.")
    })
    public ResponseEntity<RestaurantImagesResponse> getImages(@RequestParam int purposeRestId) {
        try {
            RestaurantImagesResponse restaurantImages = purposeRestImgService.showImageList(purposeRestId);
            return new ResponseEntity<>(restaurantImages, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping(value = "/create/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "식당 정보 제안 사진 등록", description = "사진을 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사진을 성공적으로 저장했습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
            @ApiResponse(responseCode = "410", description = "저장할 사진을 찾지 못해 실패했습니다."),
            @ApiResponse(responseCode = "417", description = "저장할 사진을 찾지 못해 실패했습니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류입니다.")
    })
    public ResponseEntity<RestaurantImagesResponse> createImages(@RequestParam int purposeRestId,
                                                                 @RequestPart MultipartFile[] images,
                                                                 Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();
        try {
            int userId = userService.findId(email);
            PurposeRestaurant purpose = purposeService.getPurposeRestById(purposeRestId);
            RestaurantImagesResponse restaurantImages = purposeRestImgService.save(purpose, userId, images);
            return new ResponseEntity<>(restaurantImages, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.GONE);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.EXPECTATION_FAILED);
        }
    }

    @GetMapping("/get/menu")
    @Operation(summary = "메뉴 정보 수정 제안 조회", description = "메뉴 정보 수정 제안을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "메뉴 정보 수정 제안을 성공적으로 불러왔습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다.")
    })
    public ResponseEntity<List<PurposeMenuResponse>> getMenu(@RequestParam int restaurantId) {
        try {
            List<PurposeMenuResponse> result = purposeMenuService.findListByRestaurantId(restaurantId);
            return ResponseEntity.ok().body(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/create/menu")
    @Operation(summary = "메뉴 정보 수정 제안 등록", description = "메뉴 정보 수정 제안을 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "새로운 메뉴 정보 수정 제안을 성공적으로 추가했습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다.")
    })
    public ResponseEntity<Void> createMenu(@RequestParam("restaurantId") int restaurantId,
                                            @RequestParam("name") String name,
                                            @RequestParam("price") int price,
                                            @RequestPart(value = "image", required = false) MultipartFile image,
                                            Authentication authentication) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            String email = userDetails.getUsername();
        try {
            int userId = userService.findId(email);
            PurposeMenuRequest dto = new PurposeMenuRequest(restaurantId, name, price, userId);
            purposeMenuService.register(dto, image);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/delete/menu")
    @Operation(summary = "메뉴 정보 수정 제안 삭제", description = "메뉴 정보 수정 제안을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "메뉴 정보 수정 제안을 성공적으로 삭제했습니다."),
            @ApiResponse(responseCode = "404", description = "삭제할 메뉴를 찾을 수 없습니다."),
            @ApiResponse(responseCode = "417", description = "AWS S3에서 사진 삭제에 실패했습니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류입니다.")
    })
    public ResponseEntity<Void> deleteMenu(@RequestParam int menuId) {
        try {
            purposeMenuService.delete(menuId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).build();
        }
    }
}
