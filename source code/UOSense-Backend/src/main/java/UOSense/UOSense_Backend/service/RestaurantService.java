package UOSense.UOSense_Backend.service;

import UOSense.UOSense_Backend.common.enumClass.DoorType;
import UOSense.UOSense_Backend.dto.*;
import UOSense.UOSense_Backend.entity.Restaurant;

import java.util.List;

public interface RestaurantService {
    List<RestaurantListResponse> findListByFilter(SearchService.sortFilter filter);
    List<RestaurantListResponse> findListByDoorType(DoorType doorType, SearchService.sortFilter filter);
    RestaurantResponse find(int restaurantId);
    Restaurant getRestaurantById(int restaurantId);
    List<MenuResponse> findMenu(int restaurantId);
    void registerMenu(NewMenuRequest menuRequest, String imageUrl);
    void register(RestaurantRequest restaurantRequest);
    void edit(RestaurantRequest restaurantRequest);
    void delete(int restaurantId);
    BusinessDayList findBusinessDay(int restaurantId);
    void editBusinessDay(BusinessDayList businessDayList);
    void saveBusinessDay(BusinessDayList businessDayList);
    void deleteBusinessDay(int businessDayId);
}