package UOSense.UOSense_Backend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

public interface MailService {
    /**
     * 메일 주소 형식이 올바른 형식(@uos.ac.kr로 끝나는 형식)인지 검사합니다.
     */
    boolean checkMailAddress(String mailAddress);
    /**
     * 중복되는 이메일 주소인지 검사합니다.
     */
    boolean checkDuplicatedMail(String mailAddress);
    /**
     * 인증 코드를 보낼 메일을 생성합니다.
     * @param purpose 이메일 태그 및 제목에 쓰일 전송 목적 e.g. 회원가입, 비밀번호 재설정
     */
    void sendAuthMail(String mailAddress, String purpose, String authCode);
}
