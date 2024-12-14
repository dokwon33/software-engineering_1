package UOSense.UOSense_Backend.service;

import UOSense.UOSense_Backend.common.Utils.ImageUtils;
import UOSense.UOSense_Backend.dto.CustomUserDetails;
import UOSense.UOSense_Backend.dto.NewUserRequest;
import UOSense.UOSense_Backend.dto.UserResponse;
import UOSense.UOSense_Backend.repository.UserRepository;
import UOSense.UOSense_Backend.entity.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import com.sun.jdi.request.DuplicateRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService, UserDetailsService {
    private final UserRepository userRepository;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;
    private final ImageUtils imageUtils;
    final String S3_FOLDER_NAME = "user";
    
    @Override
    public UserDetails loadUserByUsername(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            throw new UsernameNotFoundException("Invalid authentication!");
        }

        return new CustomUserDetails(user.get());
    }
    
    @Override
    @Transactional
    public User register(NewUserRequest newUserRequest) {
        // 요청 정보 검증
        if (!validatedUserInfo(newUserRequest))
            throw new IllegalArgumentException("유효하지 않은 사용자 정보입니다.");
        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(newUserRequest.getPassword());
        // DB에 저장
        return userRepository.save(newUserRequest.toEntity(encodedPassword));
    }

    private boolean validatedUserInfo(NewUserRequest newUserRequest) {
        // 웹메일 검증
        boolean validatedMail = mailService.checkDuplicatedMail(newUserRequest.getEmail()) &&
                mailService.checkMailAddress(newUserRequest.getEmail());
        // 비밀번호 검증
        boolean validatedPassword = checkPasswordFormat(newUserRequest.getPassword());
        // 닉네임 검증
        boolean validatedNickname = checkNickName(newUserRequest.getNickname());
        return validatedMail || validatedPassword || validatedNickname;
    }

    private boolean checkPasswordFormat(String password) {
      
        if (password.length() < 8 || password.length() > 20) {
            throw new IllegalArgumentException("비밀번호 자리수 제한");
        }

        // 각 조건을 확인하기 위한 플래그
        boolean hasUpperCase = false;
        boolean hasLowerCase = false;
        boolean hasNumber = false;
        boolean hasSpecialChar = false;

        // 문자열의 각 문자를 확인
        for (char ch : password.toCharArray()) {
            if (Character.isUpperCase(ch)) {
                hasUpperCase = true;
            } else if (Character.isLowerCase(ch)) {
                hasLowerCase = true;
            } else if (Character.isDigit(ch)) {
                hasNumber = true;
            } else if ("!@#$%^&*()_+-=[]{}|;:,.<>?".indexOf(ch) != -1) {
                hasSpecialChar = true;
            }
            if (hasUpperCase && hasLowerCase && hasNumber && hasSpecialChar) return true;
        }
        throw new IllegalArgumentException("비밀번호 형식 제한");
    }

    @Override
    public boolean checkNickName(String nickname) {
        if (userRepository.existsByNickname(nickname)) {
            throw new DuplicateRequestException("이미 존재하는 닉네임입니다.");
        }
        return true;
    }

    /** 비밀번호 일치 여부를 확인합니다 */
    @Override
    public boolean checkPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    @Override
    public int findId(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            throw new IllegalArgumentException("Invalid authentication!");
        }
        return user.get().getId();
    }

    @Override
    public UserResponse find(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(()-> new IllegalArgumentException("Invalid authentication!"));
        return UserResponse.from(user);
    }

    @Override
    public UserResponse update(String email, MultipartFile image, String nickname) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            throw new IllegalArgumentException("Invalid authentication!");
        }
        User user = optionalUser.get();
        String oldImageUrl = user.getImageUrl();
        String newimageUrl = null;

        // 변경할 이미지가 있을 경우
        if (!image.isEmpty()) {
            // 기존 이미지가 있을 경우 삭제
            if (oldImageUrl != null) {
                imageUtils.deleteImageInS3(oldImageUrl);
            }
            newimageUrl = imageUtils.uploadImageToS3(image, S3_FOLDER_NAME);
        }

        if (newimageUrl != null) {
            user.setImageUrl(newimageUrl);
        }

        if (nickname != null) {
            checkNickName(nickname);
            user.setNickname(nickname);
        }

        user = userRepository.saveAndFlush(user);
        return UserResponse.from(user);
    }
}
