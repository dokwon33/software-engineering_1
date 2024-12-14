package UOSense.UOSense_Backend.controller;

import UOSense.UOSense_Backend.common.Utils.RedisUtilForToken;
import UOSense.UOSense_Backend.common.enumClass.Role;
import UOSense.UOSense_Backend.dto.CustomUserDetails;
import UOSense.UOSense_Backend.dto.UserResponse;
import UOSense.UOSense_Backend.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.coyote.BadRequestException;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import UOSense.UOSense_Backend.common.Utils.JWTUtil;
import UOSense.UOSense_Backend.dto.NewUserRequest;
import UOSense.UOSense_Backend.service.UserService;
import com.sun.jdi.request.DuplicateRequestException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "회원 관리")
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService; 
    private final JWTUtil jwtUtil;
    private final RedisUtilForToken redisUtilForToken;
    @Value("${jwt.access.duration}")
    private long accessDuration;
    @Value("${jwt.refresh.duration}")
    private long refreshDuration;
  
    @GetMapping("/reissue")
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {
        String refresh;
        try {
            refresh = jwtUtil.parseCookie(request);
        } catch (BadRequestException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        Map<String, String> tokens;
        try {
            tokens = jwtUtil.createTokens(refresh);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        // 응답
        response.setHeader("access", "Bearer " + tokens.get("access"));
        response.addCookie(jwtUtil.createCookie("refresh", tokens.get("refresh")));

        Map<String, Object> message = new HashMap<>();
        message.put("message", "Reissue refresh token success");
        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    /**
     * 로그아웃 API, redis에 저장된 refresh token을 삭제
     */
    @PutMapping("/signout")
    public ResponseEntity<?> signOut(HttpServletRequest request) {
        String refresh;
        try {
            refresh = jwtUtil.parseCookie(request);
        } catch (BadRequestException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        try {
            jwtUtil.deleteRefresh(refresh);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        Map<String, Object> message = new HashMap<>();
        message.put("message", "Signed out successfully");
        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "회원을 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 회원을 등록했습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류입니다.")
    })
    /** 프론트에서 비밀번호와 비밀번호 확인 일치할 때만 요청 보냈다고 가정 */
    public ResponseEntity<Boolean> signUp(@RequestBody NewUserRequest newUserRequest, HttpServletResponse response) {
        User user;
        try {
             user = userService.register(newUserRequest);
        } catch (IllegalArgumentException | DuplicateRequestException e) {
            return ResponseEntity.badRequest().body(false);
        }
        String username = user.getEmail();
        Role role = user.getRole();

        // accessToken과 refreshToken 생성
        String accessToken = jwtUtil.createJwt("access", username, role.getValue(), accessDuration);
        String refreshToken = jwtUtil.createJwt("refresh", username, role.getValue(), refreshDuration);

        // redis에 insert (key = username / value = refreshToken)
        redisUtilForToken.setToken(username, refreshToken, Duration.ofMillis(refreshDuration));

        // 응답
        response.setHeader("access", "Bearer " + accessToken);
        response.addCookie(jwtUtil.createCookie("refresh", refreshToken));
        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE); // 응답 타입 설정

        return ResponseEntity.ok().body(true);
    }

    @PostMapping("/check-nickname")
    @Operation(summary = "닉네임 중복 검사", description = "닉네임 중복 여부를 판별합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용 가능한 닉네임입니다."),
            @ApiResponse(responseCode = "400", description = "중복되는 닉네임입니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류입니다.")
    })
    public ResponseEntity<Boolean> validateNickName (@RequestParam String nickname) {
        try {
            boolean isValidated = userService.checkNickName(nickname);
            return ResponseEntity.ok().body(isValidated);
        } catch (DuplicateRequestException e) {
            return ResponseEntity.badRequest().body(false);
        }
    }

    @PutMapping("/get")
    @Operation(summary = "사용자 마이페이지 정보 조회", description = "사용자 마이페이지 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 마이페이지 정보를 불러왔습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류입니다.")
    })
    public ResponseEntity<UserResponse> get(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();

        try {
            UserResponse response = userService.find(email);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/update")
    @Operation(summary = "사용자 마이페이지 정보 수정", description = "사용자 마이페이지 정보를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 마이페이지 정보를 수정했습니다."),
            @ApiResponse(responseCode = "400", description = "중복되는 닉네임입니다."),
            @ApiResponse(responseCode = "417", description = "AWS S3에서 사진 등록에 실패했습니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류입니다.")
    })
    public ResponseEntity<UserResponse> update(@RequestPart(value = "image", required = false) MultipartFile image,
                                               @RequestParam(required = false) String nickname,
                                               Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();

        try {
            UserResponse response = userService.update(email, image, nickname);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException | DuplicateRequestException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).build();
        }
    }
}
