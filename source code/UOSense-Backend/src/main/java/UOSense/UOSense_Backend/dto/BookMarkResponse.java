package UOSense.UOSense_Backend.dto;

import UOSense.UOSense_Backend.entity.BookMark;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class BookMarkResponse {
    private int id;

    private int userId;

    private int restaurantId;

    public static BookMarkResponse from(BookMark bookMark){
        return BookMarkResponse.builder()
                .id(bookMark.getId())
                .userId(bookMark.getUser().getId())
                .restaurantId(bookMark.getRestaurant().getId())
                .build();
    }
}
