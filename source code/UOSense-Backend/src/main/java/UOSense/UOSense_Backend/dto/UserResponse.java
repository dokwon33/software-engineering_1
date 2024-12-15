package UOSense.UOSense_Backend.dto;

import UOSense.UOSense_Backend.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class UserResponse {
    private String nickname;
    private String imageUrl;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .nickname(user.getNickname())
                .imageUrl(user.getImageUrl())
                .build();
    }
}
