package UOSense.UOSense_Backend.config;

import UOSense.UOSense_Backend.common.securityFilter.CustomAuthenticationFilter;
import UOSense.UOSense_Backend.common.Utils.JWTUtil;
import UOSense.UOSense_Backend.common.exception.CustomAccessDeniedHandler;
import UOSense.UOSense_Backend.common.exception.CustomLoginAuthenticationEntryPoint;
import UOSense.UOSense_Backend.common.handler.CustomAuthenticationFailureHandler;
import UOSense.UOSense_Backend.common.handler.CustomAuthenticationSuccessHandler;
import UOSense.UOSense_Backend.common.securityFilter.JWTFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JWTUtil jwtUtil;

    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
    private final CustomAuthenticationFailureHandler customAuthenticationFailureHandler;
    private final AuthenticationConfiguration authenticationConfiguration;

    private final CustomLoginAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        //cors disable
        http
                .cors(Customizer.withDefaults());

        //csrf disable
        http
                .csrf(AbstractHttpConfigurer::disable);

        //From 로그인 방식 disable
        http
                .formLogin(AbstractHttpConfigurer::disable);

        //HTTP Basic 인증 방식 disable
        http
                .httpBasic(AbstractHttpConfigurer::disable);

        // Filter Chain
        http
                // JWT token 인증 필터
                .addFilterBefore(new JWTFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class)
                /**
                 * Id, password를 입력받아 로그인하는 일반적인 로그인 필터
                 * JWTFilter 앞에 위치해 있으므로 모든 요청에 먼저 거치게 됨
                 */
                .addFilterBefore(customAuthenticationFilter(), JWTFilter.class);

        //exceptionhandling
        http
                .exceptionHandling(exceptionHandling ->
                        exceptionHandling
                                .authenticationEntryPoint(authenticationEntryPoint)
                                .accessDeniedHandler(accessDeniedHandler));

        //경로별 인가 작업
        // 인증이 필요없는 경로 e.g., signup -> permitAll()
        // 관리자 인증이 필요한 경로 e.g., delete, create restaurant -> hasAuthority("ADMIN")
        // 그 외는 사용자 인증이 필요한 경로 -> authenticated()
        // API URI가 정리되면 추후 정리해서 추가할 예정, 아래 경로는 예시로 참고!
        http
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/swagger", "/swagger-ui.html", "/swagger-ui/**", "/api-docs", "/api-docs/**", "/v3/api-docs/**")
                        .permitAll()
                        .requestMatchers("/api/v1/restaurant/get/**").permitAll()
                        .requestMatchers("/api/v1/restaurant/create/**").hasAuthority("ADMIN")
                        .requestMatchers("/api/v1/restaurant/update/**").hasAuthority("ADMIN")
                        .requestMatchers("/api/v1/restaurant/delete/**").hasAuthority("ADMIN")
                        .requestMatchers("/api/v1/review/create/images").permitAll()
                        .requestMatchers("/api/v1/review/get").permitAll()
                        .requestMatchers("/api/v1/review/get/list").permitAll()
                        .requestMatchers("/api/v1/review/get/user").permitAll()
                        .requestMatchers("/api/v1/review/delete").permitAll()
                        .requestMatchers("/api/v1/report/get/list").hasAuthority("ADMIN")
                        .requestMatchers("/api/v1/bookmark/get/user").permitAll()
                        .requestMatchers("/api/v1/user/signup").permitAll()
                        .requestMatchers("/api/v1/user/signout").permitAll()
                        .requestMatchers("/api/v1/user/reissue").permitAll()
                        .requestMatchers("/api/v1/user/check-nickname").permitAll()
                        .requestMatchers("/api/v1/webmail/**").permitAll()
                        .requestMatchers("/api/v1/search/**").permitAll()
                        .requestMatchers("/api/v1/purpose/get/**").hasAuthority("ADMIN")
                        .requestMatchers("/api/v1/purpose/delete/**").hasAuthority("ADMIN")
                        .anyRequest().authenticated());

        //세션 설정 : STATELESS
        http
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();

    }

    @Bean
    public CustomAuthenticationFilter customAuthenticationFilter() throws Exception {
        CustomAuthenticationFilter customAuthenticationFilter = new CustomAuthenticationFilter();
        customAuthenticationFilter.setAuthenticationManager(authenticationManager());
        customAuthenticationFilter.setAuthenticationSuccessHandler(customAuthenticationSuccessHandler);
        customAuthenticationFilter.setAuthenticationFailureHandler(customAuthenticationFailureHandler);

        return customAuthenticationFilter;
    }

    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     *  password 암호화
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
