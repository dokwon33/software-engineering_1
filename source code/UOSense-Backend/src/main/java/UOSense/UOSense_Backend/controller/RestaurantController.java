package UOSense.UOSense_Backend.controller;

import UOSense.UOSense_Backend.common.enumClass.DoorType;
import UOSense.UOSense_Backend.dto.*;
import UOSense.UOSense_Backend.entity.Restaurant;
import UOSense.UOSense_Backend.service.MenuService;
import UOSense.UOSense_Backend.service.RestaurantImageService;
import UOSense.UOSense_Backend.service.RestaurantService;

import UOSense.UOSense_Backend.service.SearchService;
import com.amazonaws.SdkClientException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

@Tag(name = "식당 관리")
@RestController
@RequestMapping("/api/v1/restaurant")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;
    private final MenuService menuService;
    private final RestaurantImageService restaurantImageService;

    @PostMapping("/create")
    @Operation(summary = "신규 식당 등록", description = "새로운 식당을 추가합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "새로운 식당을 성공적으로 추가했습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다.")
    })
    public ResponseEntity<Void> createRestaurant(@RequestBody RestaurantRequest restaurantRequest) {
        if (restaurantRequest.getId() != -1) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        try {
            restaurantService.register(restaurantRequest);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/update")
    @Operation(summary = "기존 식당 정보 수정", description = "기존 식당 정보를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "기존 식당 정보를 성공적으로 수정했습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
            @ApiResponse(responseCode = "404", description = "식당 정보를 찾을 수 없습니다.")
    })
    public ResponseEntity<Void> updateRestaurant(@RequestBody RestaurantRequest restaurantRequest) {
        if (restaurantRequest.getId() == -1) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        try {
            restaurantService.edit(restaurantRequest);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/get/list")
    @Operation(summary = "식당 정보 일괄 조회", description = "식당 리스트를 불러옵니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "식당 리스트를 성공적으로 불러왔습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
            @ApiResponse(responseCode = "404", description = "식당 리스트를 찾을 수 없습니다.")
    })
    public ResponseEntity<List<RestaurantListResponse>> getRestaurantList(@RequestParam(required = false) DoorType doorType,
                                                                          @RequestParam SearchService.sortFilter filter) {
        List<RestaurantListResponse> restaurantList;
        boolean doorTypeFlag;

        if (Arrays.asList(DoorType.values()).contains(doorType)) {
            doorTypeFlag = true;
        }
        else if (doorType == null) {
            doorTypeFlag = false;
        }
        else { // enum에 없는 element
            return new ResponseEntity<>(Collections.emptyList(), HttpStatus.BAD_REQUEST);
        }

        try {
            if (doorTypeFlag) {
                restaurantList = restaurantService.findListByDoorType(doorType, filter);
            }
            else {
                restaurantList = restaurantService.findListByFilter(filter);
            }

            return new ResponseEntity<>(restaurantList, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(Collections.emptyList(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/get")
    @Operation(summary = "식당 정보 조회", description = "식당 정보를 불러옵니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "식당 정보를 성공적으로 불러왔습니다."),
            @ApiResponse(responseCode = "404", description = "식당을 찾을 수 없습니다.")
    })
    public ResponseEntity<RestaurantResponse> getRestaurant(@RequestParam int restaurantId) {
        try {
            RestaurantResponse restaurantResponse = restaurantService.find(restaurantId);
            return new ResponseEntity<>(restaurantResponse, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/delete")
    @Operation(summary = "식당 삭제", description = "식당을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "식당을 성공적으로 삭제했습니다."),
            @ApiResponse(responseCode = "404", description = "삭제할 식당을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "500", description = "잘못된 요청입니다.")
    })
    public ResponseEntity<Void> deleteRestaurant(@RequestParam int restaurantId) {
        try {
            restaurantService.delete(restaurantId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/get/images")
    @Operation(summary = "식당 사진 조회", description = "식당 사진을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "식당 사진을 성공적으로 불러왔습니다."),
            @ApiResponse(responseCode = "404", description = "식당 사진을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "500", description = "잘못된 요청입니다.")
    })
    public ResponseEntity<RestaurantImagesResponse> getImages(@RequestParam int restaurantId) {
        try {
            RestaurantImagesResponse restaurantImages = restaurantImageService.showImageList(restaurantId);
            return new ResponseEntity<>(restaurantImages, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping(value = "/create/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "식당 사진 등록", description = "사진을 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사진을 성공적으로 저장했습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
            @ApiResponse(responseCode = "410", description = "저장할 사진을 찾지 못해 실패했습니다."),
            @ApiResponse(responseCode = "417", description = "저장할 사진을 찾지 못해 실패했습니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류입니다.")
    })
    public ResponseEntity<RestaurantImagesResponse> createImages(
            @RequestParam int restaurantId,
            @RequestPart MultipartFile[] images) {
        try {
            Restaurant restaurant = restaurantService.getRestaurantById(restaurantId);
            RestaurantImagesResponse restaurantImages = restaurantImageService.save(restaurant, images);
            return new ResponseEntity<>(restaurantImages, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.GONE);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.EXPECTATION_FAILED);
        }
    }

    @DeleteMapping("/delete/images")
    @Operation(summary = "식당 사진 삭제", description = "사진을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사진을 성공적으로 삭제했습니다."),
            @ApiResponse(responseCode = "404", description = "삭제할 사진을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "417", description = "AWS S3에서 삭제에 실패했습니다.")
    })
    public ResponseEntity<Void> deleteImage(@RequestParam int imageId) {
        try {
            restaurantImageService.delete(imageId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (SdkClientException e) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).build();
        }
    }

    @GetMapping("/get/menu")
    @Operation(summary = "식당 메뉴 조회", description = "메뉴를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "메뉴를 성공적으로 불러왔습니다."),
            @ApiResponse(responseCode = "404", description = "메뉴를 찾을 수 없습니다."),
            @ApiResponse(responseCode = "500", description = "잘못된 요청입니다.")
    })
    public ResponseEntity<List<MenuResponse>> getMenuList(@RequestParam int restaurantId) {
        try {
            List<MenuResponse> result = restaurantService.findMenu(restaurantId);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(Collections.emptyList(), HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping(value = "/create/menu", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "식당 메뉴 등록", description = "메뉴를 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "메뉴를 성공적으로 업로드하였습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
            @ApiResponse(responseCode = "500", description = "잘못된 요청입니다.")
    })
    public ResponseEntity<String> createMenu(@RequestParam("restaurantId") int restaurantId,
                                             @RequestParam("name") String name,
                                             @RequestParam("price") int price,
                                             @RequestParam(value = "description", required = false) String description,
                                             @RequestPart(value = "image", required = false) MultipartFile image) {
        try {
            NewMenuRequest dto = new NewMenuRequest(restaurantId, name, price, description);
            String imageUrl = menuService.saveImage(image);
            restaurantService.registerMenu(dto, imageUrl);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        return ResponseEntity.ok().build();
    }

    @PutMapping("/update/menu")
    @Operation(summary = "식당 메뉴 수정", description = "메뉴를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "메뉴를 성공적으로 수정하였습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
            @ApiResponse(responseCode = "404", description = "수정할 메뉴와 등록된 식당을 확인할 수 없습니다."),
            @ApiResponse(responseCode = "500", description = "잘못된 요청입니다.")
    })
    public ResponseEntity<Void> updateMenu(@RequestParam("id") int id,
                                           @RequestParam("restaurantId") int restaurantId,
                                           @RequestParam("name") String name,
                                           @RequestParam("price") int price,
                                           @RequestParam("description") String description,
                                           @RequestPart(value = "image", required = false) MultipartFile image) {
        try {
            Restaurant restaurant = restaurantService.getRestaurantById(restaurantId);
            MenuRequest menuRequest = new MenuRequest(id,restaurantId,name,price,description);
            menuService.edit(menuRequest, image, restaurant);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/delete/menu")
    @Operation(summary = "메뉴 삭제", description = "메뉴를 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "메뉴를 성공적으로 삭제했습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
            @ApiResponse(responseCode = "404", description = "삭제할 메뉴를 찾을 수 없습니다."),
            @ApiResponse(responseCode = "417", description = "AWS S3에서 사진 삭제에 실패했습니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류입니다.")
    })
    public ResponseEntity<Void> deleteMenu(@RequestParam int menuId) {
        try {
            menuService.delete(menuId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).build();
        }
    }

    @GetMapping("/get/businessday")
    @Operation(summary = "식당 영업 정보 조회", description = "특정 식당의 영업 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "영업 정보를 성공적으로 조회했습니다."),
            @ApiResponse(responseCode = "404", description = "식당의 영업 정보를 찾을 수 없습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다.")
    })
    public ResponseEntity<BusinessDayList> getBusinessDayList(@RequestParam int restaurantId) {
        try {
            BusinessDayList businessDayList = restaurantService.findBusinessDay(restaurantId);
            return new ResponseEntity<>(businessDayList, HttpStatus.OK);
        } catch(IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/update/businessday")
    @Operation(summary = "식당 영업 정보 수정", description = "특정 식당의 영업 정보를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "영업 정보를 성공적으로 수정했습니다."),
            @ApiResponse(responseCode = "404", description = "식당의 영업 정보를 찾을 수 없습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다.")
    })
    public ResponseEntity<Void> updateBusinessDayList(@RequestBody BusinessDayList businessDayList) {
        try {
            restaurantService.editBusinessDay(businessDayList);
            return ResponseEntity.ok().build();
        } catch(IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/create/businessday")
    @Operation(summary = "식당 영업 정보 등록", description = "특정 식당의 영업 정보를 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "영업 정보를 성공적으로 등록했습니다."),
            @ApiResponse(responseCode = "404", description = "식당 정보를 찾을 수 없습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다.")
    })
    public ResponseEntity<Void> createBusinessDayList(@RequestBody BusinessDayList businessDayList) {
        try {
            restaurantService.saveBusinessDay(businessDayList);
            return ResponseEntity.ok().build();
        } catch(NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/delete/businessday")
    @Operation(summary = "영업 정보 삭제", description = "영업 정보를 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "영업 정보를 성공적으로 삭제했습니다."),
            @ApiResponse(responseCode = "404", description = "삭제할 영업 정보가 존재하지 않습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다.")
    })
    public ResponseEntity<Void> deleteBusinessDay(@RequestParam int businessDayId) {
        try {
            restaurantService.deleteBusinessDay(businessDayId);
            return ResponseEntity.ok().build();
        } catch(IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
