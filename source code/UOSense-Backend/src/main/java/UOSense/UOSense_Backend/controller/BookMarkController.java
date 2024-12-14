package UOSense.UOSense_Backend.controller;

import UOSense.UOSense_Backend.dto.BookMarkResponse;
import UOSense.UOSense_Backend.dto.CustomUserDetails;
import UOSense.UOSense_Backend.service.BookMarkService;
import UOSense.UOSense_Backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@Tag(name = "즐겨찾기 관리")
@RestController
@RequestMapping("/api/v1/bookmark")
@RequiredArgsConstructor
public class BookMarkController {
    private final BookMarkService bookMarkService;
    private final UserService userService;

    @PostMapping("/create")
    @Operation(summary = "특정 식당 즐겨찾기 등록", description = "즐겨찾기 목록에 식당을 추가합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "즐겨찾기에 식당을 성공적으로 추가했습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류입니다.")
    })
    public ResponseEntity<Void> create(@RequestParam int restaurantId, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();

        try {
            int userId = userService.findId(email);
            bookMarkService.register(userId, restaurantId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete")
    @Operation(summary = "특정 식당 즐겨찾기 삭제", description = "즐겨찾기 목록에 식당을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "즐겨찾기에 식당을 성공적으로 삭제했습니다."),
            @ApiResponse(responseCode = "400", description = "삭제할 즐겨찾기가 존재하지 않습니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류입니다.")
    })
    public ResponseEntity<Void> delete(@RequestParam int restaurantId, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();

        try {
            int userId = userService.findId(email);
            bookMarkService.remove(userId, restaurantId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/get/user")
    @Operation(summary = "다른 사용자 즐겨찾기 조회", description = "다른 사용자의 즐겨찾기 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 즐겨찾기 목록을 조회했습니다."),
            @ApiResponse(responseCode = "404", description = "북마크 정보가 없습니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류입니다.")
    })
    public ResponseEntity<List<BookMarkResponse>> getByUserId(@RequestParam int userId) {
        try {
            List<BookMarkResponse> bookMarkList = bookMarkService.find(userId);
            return new ResponseEntity<>(bookMarkList,HttpStatus.OK);
        } catch(IllegalArgumentException e) {
            return new ResponseEntity<>(Collections.emptyList(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/get/mine")
    @Operation(summary = "자신 즐겨찾기 조회", description = "자신의 즐겨찾기 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 즐겨찾기 목록을 조회했습니다."),
            @ApiResponse(responseCode = "404", description = "북마크 정보가 없습니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류입니다.")
    })
    public ResponseEntity<List<BookMarkResponse>> get(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();

        try {
            int userId = userService.findId(email);
            List<BookMarkResponse> bookMarkList = bookMarkService.find(userId);
            return new ResponseEntity<>(bookMarkList,HttpStatus.OK);
        } catch(IllegalArgumentException e) {
            return new ResponseEntity<>(Collections.emptyList(), HttpStatus.NOT_FOUND);
        }
    }
}
