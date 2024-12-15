package UOSense.UOSense_Backend.dto;

import UOSense.UOSense_Backend.entity.BusinessDay;
import UOSense.UOSense_Backend.entity.Restaurant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class BusinessDayInfo {
    private int id;

    private BusinessDay.DayOfWeek dayOfWeek;

    private boolean breakTime;

    private String startBreakTime;

    private String stopBreakTime;

    private String openingTime;

    private String closingTime;

    private boolean holiday;

    public void setBreakTime() {
        this.startBreakTime = null;
        this.stopBreakTime = null;
    }

    public void setTime() {
        this.startBreakTime = null;
        this.stopBreakTime = null;
        this.openingTime = null;
        this.closingTime = null;
    }

    public static BusinessDayInfo from(BusinessDay businessDay) {
        return BusinessDayInfo.builder()
                .id(businessDay.getId())
                .dayOfWeek(businessDay.getDayOfWeek())
                .breakTime(businessDay.isBreakTime())
                .startBreakTime(String.valueOf(businessDay.getStartBreakTime()))
                .stopBreakTime(String.valueOf(businessDay.getStopBreakTime()))
                .openingTime(String.valueOf(businessDay.getOpeningTime()))
                .closingTime(String.valueOf(businessDay.getClosingTime()))
                .holiday(businessDay.isHoliday())
                .build();
    }

    public static BusinessDay toEntity(BusinessDayInfo businessDayInfo, Restaurant restaurant) {
        LocalTime startBreakTime = (businessDayInfo.getStartBreakTime() != null) ? LocalTime.parse(businessDayInfo.getStartBreakTime()) : null;
        LocalTime stopBreakTime = (businessDayInfo.getStopBreakTime() != null) ? LocalTime.parse(businessDayInfo.getStopBreakTime()) : null;
        LocalTime openingTime = (businessDayInfo.getOpeningTime() != null) ? LocalTime.parse(businessDayInfo.getOpeningTime()) : null;
        LocalTime closingTime = (businessDayInfo.getClosingTime() != null) ? LocalTime.parse(businessDayInfo.getClosingTime()) : null;
        BusinessDay businessDay = BusinessDay.builder()
                .restaurant(restaurant)
                .dayOfWeek(businessDayInfo.getDayOfWeek())
                .breakTime(businessDayInfo.isBreakTime())
                .startBreakTime(startBreakTime)
                .stopBreakTime(stopBreakTime)
                .openingTime(openingTime)
                .closingTime(closingTime)
                .holiday(businessDayInfo.isHoliday())
                .build();
        if (businessDayInfo.getId() == -1) {
            // 신규 엔티티이므로 id 필드를 비워둠.
            return businessDay;
        }
        else {
            businessDay.setId(businessDayInfo.getId());
            return businessDay;
        }
    }
}
