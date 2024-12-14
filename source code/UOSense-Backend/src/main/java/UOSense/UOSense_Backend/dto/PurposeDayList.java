package UOSense.UOSense_Backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class PurposeDayList {
    private int restaurantId;

    private List<PurposeDayInfo> purposeDayInfoList;

}
