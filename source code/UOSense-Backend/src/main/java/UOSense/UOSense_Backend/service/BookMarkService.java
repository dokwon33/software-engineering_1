package UOSense.UOSense_Backend.service;


import UOSense.UOSense_Backend.dto.BookMarkResponse;

import java.util.List;

public interface BookMarkService {
    /**
     * 즐겨찾기 등록
     * @param userId
     * @param restaurantId
     * */
    void register(int userId, int restaurantId);
    /**
     * 즐겨찾기 삭제
     * @param userId
     * @param restaurantId
     * */
    void remove(int userId, int restaurantId);
    /**
     * 즐겨찾기 목록 조회
     * @param userId
     * @return List(BookMarkResponse)
     * */
    List<BookMarkResponse> find(int userId);
}
