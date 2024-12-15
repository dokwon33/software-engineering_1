package UOSense.UOSense_Backend.common.securityFilter;

import UOSense.UOSense_Backend.common.Utils.JWTUtil;
import UOSense.UOSense_Backend.common.enumClass.Role;
import UOSense.UOSense_Backend.dto.CustomUserDetails;
import UOSense.UOSense_Backend.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 요청 당 한 번만 거치는 JWT 검증 필터
 */
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        // 요청 헤더에서 key == "access"인 헤더의 value를 가져옴 -> accessToken
        String accessToken = request.getHeader("access");

        // 요청헤더에 access가 없는 경우 인증 과정 종료
        if (accessToken == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // Bearer 제거 <- oAuth2를 이용했다고 명시적으로 붙여주는 타입인데 JWT를 검증하거나 정보를 추출 시 제거해줘야한다.
        String originToken = accessToken.substring(7);

        // category 파싱 및 토큰 유효 여부 확인
        String category;
        try {
            category = jwtUtil.getCategory(originToken);
        } catch (ExpiredJwtException e) {
            // 파싱할 때 JWT 토큰이 만료되었는 지 확인함.
            // 토큰 기한이 만료되었으면,
            // HTTP 응답 상태 코드를 401 (Unauthorized)로 설정합니다.
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            Map<String, Object> msg = new HashMap<>();
            msg.put("message", "Access token expired");
            objectMapper.writeValue(response.getWriter(), msg);
            return;
        }

        // JWTFilter는 요청에 대해 accessToken만 취급하므로 access인지 확인
        if (!"access".equals(category)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            Map<String, Object> msg = new HashMap<>();
            msg.put("message", "Invalid access token");
            objectMapper.writeValue(response.getWriter(), msg);
            return;
        }

        // 사용자명과 권한을 accessToken에서 추출
        User user = User.builder()
                .email(jwtUtil.getEmail(originToken))
                .password(null)
                .nickname(null)
                .imageUrl(null)
                .role(Role.getRole(jwtUtil.getRole(originToken)))
                .build();

        CustomUserDetails customUser = new CustomUserDetails(user);

        Authentication authentication = new UsernamePasswordAuthenticationToken(customUser, null, customUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}
