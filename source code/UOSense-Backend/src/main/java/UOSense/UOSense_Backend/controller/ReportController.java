package UOSense.UOSense_Backend.controller;

import UOSense.UOSense_Backend.dto.CustomUserDetails;
import UOSense.UOSense_Backend.dto.ReportRequest;
import UOSense.UOSense_Backend.dto.ReportResponse;
import UOSense.UOSense_Backend.service.ReportService;
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

import java.util.List;

@Tag(name = "신고 관리")
@RestController
@RequestMapping("/api/v1/report")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;
    private final UserService userService;

    @PostMapping("/create/review")
    @Operation(summary = "리뷰 신고", description = "리뷰를 신고합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 리뷰를 신고했습니다."),
            @ApiResponse(responseCode = "404", description = "리뷰 혹은 유저 정보를 찾을 수 없습니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류입니다.")
    })
    public ResponseEntity<Void> create(@RequestBody ReportRequest reportRequest, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();

        try {
            int userId = userService.findId(email);
            reportService.register(reportRequest, userId);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/get/list")
    @Operation(summary = "관리자 신고 일괄 조회", description = "관리자 권한으로 모든 신고를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "모든 신고 건을 성공적으로 불러왔습니다."),
            @ApiResponse(responseCode = "403", description = "접근 권한이 없습니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류입니다.")
    })
    public ResponseEntity<List<ReportResponse>> getList() {
        List<ReportResponse> reportResponses = reportService.findList();
        return new ResponseEntity<>(reportResponses, HttpStatus.OK);
    }
}
