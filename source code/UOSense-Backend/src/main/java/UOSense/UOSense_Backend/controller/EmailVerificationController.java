package UOSense.UOSense_Backend.controller;

import UOSense.UOSense_Backend.dto.AuthCodeRequest;
import UOSense.UOSense_Backend.dto.WebmailRequest;
import UOSense.UOSense_Backend.service.AuthService;
import UOSense.UOSense_Backend.service.MailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@Tag(name = "웹메일 인증 관리")
@RestController
@RequestMapping("/api/v1/webmail")
@RequiredArgsConstructor
public class EmailVerificationController {
    private final AuthService authService;
    private final MailService mailService;

    @GetMapping("/check-format")
    @Operation(summary = "웹메일 주소 검증", description = "웹메일 주소를 검증합니다. (주소 형식, 중복 확인)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "올바른 웹메일 주소입니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
            @ApiResponse(responseCode = "500", description = "네트워크 연결에 문제가 있습니다.")
    })
    public ResponseEntity<Boolean> validateWebMail(@RequestParam String mailAddress) {
        try {
            if (mailService.checkMailAddress(mailAddress) && mailService.checkDuplicatedMail(mailAddress))
                return ResponseEntity.status(HttpStatus.OK).body(true);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(false);
    }

    @PostMapping("/verify")
    @Operation(summary = "인증번호 전송", description = "웹메일로 인증번호를 전송합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 메일을 전송했습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
            @ApiResponse(responseCode = "500", description = "네트워크 연결에 문제가 있습니다.")
    })
    public ResponseEntity<Boolean> sendAuthCode(@RequestBody WebmailRequest webmailRequest) {
        try {
            String email = webmailRequest.getEmail();
            String purpose = webmailRequest.getPurpose();
            String code = authService.createAuthCode();
            mailService.sendAuthMail(email, purpose, code);
            authService.saveAuthCode(email, code);
            return ResponseEntity.status(HttpStatus.OK).body(true);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(false);
        }
    }

    @PostMapping("/authenticate-code")
    @Operation(summary = "인증번호 확인", description = "인증번호를 검증합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "올바른 인증코드입니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
            @ApiResponse(responseCode = "500", description = "네트워크 연결에 문제가 있습니다.")
    })
    public ResponseEntity<Boolean> validateCode(@RequestBody AuthCodeRequest authCodeRequest) {
        try {
            if (authService.checkAuthCode(authCodeRequest.getEmail(),authCodeRequest.getCode()))
                return ResponseEntity.status(HttpStatus.OK).body(true);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(false);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(false);
    }
}
