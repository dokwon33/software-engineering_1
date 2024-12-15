package UOSense.UOSense_Backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Entity
@Table(name = "Purpose_BusinessDay")
public class PurposeBusinessDay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)  // Restaurant와 다대일 관계
    @JoinColumn(name = "restaurant_id", nullable = false)  // 외래키 명시
    private Restaurant restaurant;

    @Column(name = "day_of_week", nullable = false)
    @Enumerated(EnumType.STRING)
    private BusinessDay.DayOfWeek dayOfWeek;

    @Column(name = "have_break_time", nullable = false)
    private boolean breakTime;

    @Column(name = "start_break_time")
    private LocalTime startBreakTime;

    @Column(name = "stop_break_time")
    private LocalTime stopBreakTime;

    @Column(name = "opening_time")
    private LocalTime openingTime;

    @Column(name ="closing_time")
    private LocalTime closingTime;

    @Column(name = "is_holiday", nullable = false)
    private boolean holiday;

    @ManyToOne(fetch = FetchType.LAZY)  // User와 다대일 관계
    @JoinColumn(name = "user_id", nullable = false)  // 외래키 명시
    private User user;

    public void setId(int id) {
        this.id = id;
    }
}
