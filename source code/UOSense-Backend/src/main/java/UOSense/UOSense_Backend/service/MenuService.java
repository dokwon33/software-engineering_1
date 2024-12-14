package UOSense.UOSense_Backend.service;

import UOSense.UOSense_Backend.dto.MenuRequest;
import UOSense.UOSense_Backend.entity.Restaurant;
import org.springframework.web.multipart.MultipartFile;

public interface MenuService {
    /** 스토리지에 이미지 저장 후 URL을 반환합니다. */
    String saveImage(MultipartFile file);
    /** 메뉴를 새로 덮어씌워 저장합니다. */
    void edit(MenuRequest menuRequest, MultipartFile image, Restaurant restaurant);
    void delete (int id);
}
