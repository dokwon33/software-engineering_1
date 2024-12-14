package UOSense.UOSense_Backend.service;

import UOSense.UOSense_Backend.dto.NewUserRequest;
import UOSense.UOSense_Backend.dto.UserResponse;
import UOSense.UOSense_Backend.entity.User;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    boolean checkNickName(String nickname);
    User register(NewUserRequest newUserRequest);
    boolean checkPassword(String rawPassword, String encodedPassword);
    int findId(String email);
    UserResponse find(String email);
    UserResponse update(String email, MultipartFile image, String nickname);
}