package UOSense.UOSense_Backend.dto;

import UOSense.UOSense_Backend.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final User user;

    // 사용자에게 부여된 권한(authorities) 목록을 반환
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> roles = new ArrayList<>();
        roles.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return user.getRole().getValue();
            }
        });
        return roles;
    }

    @Override
    public String getPassword() { // 사용자의 암호화된 비밀번호를 반환
        return user.getPassword();
    }

    @Override
    public String getUsername() { // 사용자의 고유 식별자인 email을 반환
        return user.getEmail();
    }

    // 계정이 만료 되었는지 (true: 만료X)
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // 계정이 잠겼는지 (true: 잠기지 않음)
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // 비밀번호가 만료되었는지 (true: 만료X)
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // 계정이 활성화(사용가능)인지 (true: 활성화)
    @Override
    public boolean isEnabled() {
        return true;
    }

    public User getUser() {
        return user;
    }
}