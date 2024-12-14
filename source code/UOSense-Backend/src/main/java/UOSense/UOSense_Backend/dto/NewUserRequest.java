package UOSense.UOSense_Backend.dto;

import UOSense.UOSense_Backend.common.enumClass.Role;
import UOSense.UOSense_Backend.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class NewUserRequest {
    private String email;

    private String password;

    private String nickname;

    public User toEntity(String encodedPw) {
        return User.builder()
                .email(this.getEmail())
                .password(encodedPw)
                .nickname(this.getNickname())
                .role(Role.USER)
                .build();
    }
}
