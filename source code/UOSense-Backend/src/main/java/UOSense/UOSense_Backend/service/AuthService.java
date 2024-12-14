package UOSense.UOSense_Backend.service;

public interface AuthService {
    String createAuthCode();
    void saveAuthCode( String email, String authCode);
    /**인증코드의 유효성을 검사합니다.
     * 해당 메일 인증코드 전송 여부와 사용자가 입력한 인증코드가 전송한 인증코드와 일치하는지 확인합니다.*/
    boolean checkAuthCode(String email, String authCode);
}
