package UOSense.UOSense_Backend.common.handler;

import UOSense.UOSense_Backend.common.Utils.RedisUtilForToken;
import UOSense.UOSense_Backend.common.enumClass.Role;
import UOSense.UOSense_Backend.dto.CustomUserDetails;
import UOSense.UOSense_Backend.common.Utils.JWTUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;
    private final RedisUtilForToken redisUtilForToken;
    private final ObjectMapper objectMapper;

    @Value("${jwt.access.duration}")
    private long accessDuration;
    @Value("${jwt.refresh.duration}")
    private long refreshDuration;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        UserDetails userDetail = (CustomUserDetails) authentication.getPrincipal();

        String username = userDetail.getUsername();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();

        // accessToken과 refreshToken 생성
        String accessToken = jwtUtil.createJwt("access", username, role, accessDuration);
        String refreshToken = jwtUtil.createJwt("refresh", username, role, refreshDuration);

        // redis에 insert (key = username / value = refreshToken)
        redisUtilForToken.setToken(username, refreshToken, Duration.ofMillis(refreshDuration));

        // 응답
        Map<String, Object> msg = new HashMap<>();
        msg.put("message", "Login success");
        response.setHeader("access", "Bearer " + accessToken);
        response.addCookie(jwtUtil.createCookie("refresh", refreshToken));
        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE); // 응답 타입 설정

        objectMapper.writeValue(response.getWriter(), msg);
    }
}