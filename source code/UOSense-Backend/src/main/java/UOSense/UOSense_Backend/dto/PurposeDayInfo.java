package UOSense.UOSense_Backend.dto;

import UOSense.UOSense_Backend.entity.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class PurposeDayInfo {
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

    public static PurposeDayInfo from(PurposeBusinessDay purposeBusinessDay) {
        return PurposeDayInfo.builder()
                .id(purposeBusinessDay.getId())
                .dayOfWeek(purposeBusinessDay.getDayOfWeek())
                .breakTime(purposeBusinessDay.isBreakTime())
                .startBreakTime(String.valueOf(purposeBusinessDay.getStartBreakTime()))
                .stopBreakTime(String.valueOf(purposeBusinessDay.getStopBreakTime()))
                .openingTime(String.valueOf(purposeBusinessDay.getOpeningTime()))
                .closingTime(String.valueOf(purposeBusinessDay.getClosingTime()))
                .holiday(purposeBusinessDay.isHoliday())
                .build();
    }

    public static PurposeBusinessDay toEntity(PurposeDayInfo purposeDayInfo, Restaurant restaurant, User user) {
        LocalTime startBreakTime = (purposeDayInfo.getStartBreakTime() != null) ? LocalTime.parse(purposeDayInfo.getStartBreakTime()) : null;
        LocalTime stopBreakTime = (purposeDayInfo.getStopBreakTime() != null) ? LocalTime.parse(purposeDayInfo.getStopBreakTime()) : null;
        LocalTime openingTime = (purposeDayInfo.getOpeningTime() != null) ? LocalTime.parse(purposeDayInfo.getOpeningTime()) : null;
        LocalTime closingTime = (purposeDayInfo.getClosingTime() != null) ? LocalTime.parse(purposeDayInfo.getClosingTime()) : null;
        return PurposeBusinessDay.builder()
                .restaurant(restaurant)
                .dayOfWeek(purposeDayInfo.getDayOfWeek())
                .breakTime(purposeDayInfo.isBreakTime())
                .startBreakTime(startBreakTime)
                .stopBreakTime(stopBreakTime)
                .openingTime(openingTime)
                .closingTime(closingTime)
                .holiday(purposeDayInfo.isHoliday())
                .user(user)
                .build();
    }
}
