package UOSense.UOSense_Backend.common.Utils;

import UOSense.UOSense_Backend.common.enumClass.Role;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JWTUtil {

    private final SecretKey secretKey;
    private final RedisUtilForToken redisUtilForToken;
    @Value("${jwt.access.duration}")
    private long accessDuration;
    @Value("${jwt.refresh.duration}")
    private long refreshDuration;

    // 암호화 알고리즘은 HS256을 사용
    public JWTUtil(@Value("${jwt.secret}") String secret, RedisUtilForToken redisUtilForToken) {
        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
        this.redisUtilForToken = redisUtilForToken;
    }

    /**
     * 사용자명 추출
     */
    public String getEmail(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("email", String.class);
    }

    /**
     * 권한 추출
     */
    public String getRole(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("role", String.class);
    }

    /**
     * token 유효확인. true면 만료, false면 유효.
     */
    public Boolean isExpired(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getExpiration().before(new Date());
    }

    /**
     * accessToken인지 refreshToken인지 확인
     */
    public String getCategory(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("category", String.class);
    }

    /**
     * JWT 발급
     */
    public String createJwt(String category, String email, String role, Long expiredMs) {
        return Jwts.builder()
                .claim("category", category)
                .claim("email", email)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();
    }

    public Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24*60*60);     // 쿠키가 살아있을 시간
        //cookie.setSecure();          // https에서만 동작할것인지 (로컬은 http 환경이라 안먹음, 배포환경에서만 활성화)
        //cookie.setPath("/");        // 쿠키가 전역 경로에서 동작, 아니면 특정 경로에서만 동작하게 제한할 수 있음
        //cookie.setHttpOnly(true);       // http에서만 쿠키가 동작할 수 있도록 (js에서 접근할 수 없음)

        return cookie;
    }

    public String parseCookie(HttpServletRequest request) throws BadRequestException {
        // 쿠키에 존재하는 refreshToken을 가져오자
        String refresh = null;
        Cookie[] cookies = request.getCookies();
        if(cookies == null) {
            throw new BadRequestException("Cookies is null");
        }
        for(Cookie cookie : cookies) {
            if("refresh".equals(cookie.getName())) {
                refresh = cookie.getValue();
            }
        }

        // 검증 시작
        // refreshToken이 없는 경우
        if(refresh == null) {
            throw new BadRequestException("Refresh token is null");
        }

        // 유효기간 확인
        // 토큰이 refresh인지 확인
        String category;
        try {
            category = getCategory(refresh);
        } catch (ExpiredJwtException e) {
            throw new BadRequestException("Refresh token is expired");
        }

        if(!category.equals("refresh")) {
            throw new BadRequestException("Invalid refresh token");
        }

        return refresh;
    }

    public Map<String, String> createTokens(String refresh) {
        // 새로운 Token을 만들기 위해 준비
        String email = getEmail(refresh);
        String role = getRole(refresh);

        // Redis내에 존재하는 refreshToken인지 확인
        if(!redisUtilForToken.checkExistsToken(email)) {
            throw new IllegalArgumentException("No exists in redis refresh token");
        }

        // 새로운 JWT Token 생성
        String newAccessToken = createJwt("access", email, role, accessDuration);
        String newRefreshToken = createJwt("refresh", email, role, refreshDuration);

        // update refreshToken to Redis
        redisUtilForToken.setToken(email, newRefreshToken, Duration.ofMillis(refreshDuration));

        Map<String, String> tokens = new HashMap<>();
        tokens.put("access", newAccessToken);
        tokens.put("refresh", newRefreshToken);
        return tokens;
    }

    public void deleteRefresh(String refresh) {
        String key = getEmail(refresh);

        if(!redisUtilForToken.checkExistsToken(key)) {
            throw new IllegalArgumentException("No exists in redis refresh token");
        }

        redisUtilForToken.deleteToken(key);
    }
}