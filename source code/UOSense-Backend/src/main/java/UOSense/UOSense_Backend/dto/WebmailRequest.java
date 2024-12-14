package UOSense.UOSense_Backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class WebmailRequest {
    private String email;   // 웹메일 주소
    private String purpose; // 이메일 태그 및 제목에 쓰일 전송 목적 e.g. 회원가입, 비밀번호 재설정
}
