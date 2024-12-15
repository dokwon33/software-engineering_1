package UOSense.UOSense_Backend.service;

import UOSense.UOSense_Backend.dto.PurposeDayList;

public interface PurposeDayService {
    PurposeDayList find(int restaurantId);
    void register(PurposeDayList purposeDayList, int userId);
    void delete(int purposeDayId);
}
