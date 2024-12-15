package UOSense.UOSense_Backend.service;

import UOSense.UOSense_Backend.dto.*;
import UOSense.UOSense_Backend.entity.PurposeRestaurant;

import java.util.List;

public interface PurposeService {
    void register(PurposeRestRequest request, int userId);
    List<PurposeRestResponse> findList(int restaurantId);
    void delete(int purposeRestId);
    PurposeRestaurant getPurposeRestById(int restaurantId);
}
