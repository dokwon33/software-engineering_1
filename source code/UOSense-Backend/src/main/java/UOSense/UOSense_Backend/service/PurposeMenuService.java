package UOSense.UOSense_Backend.service;

import UOSense.UOSense_Backend.dto.PurposeMenuRequest;
import UOSense.UOSense_Backend.dto.PurposeMenuResponse;
import UOSense.UOSense_Backend.entity.PurposeMenu;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PurposeMenuService {
    List<PurposeMenuResponse> findListByRestaurantId(int restaurantId);
    public
    void register(PurposeMenuRequest request, MultipartFile menuImage);
    String saveImage(MultipartFile menuImage);
    void delete(int PurposeMenuId);
}
