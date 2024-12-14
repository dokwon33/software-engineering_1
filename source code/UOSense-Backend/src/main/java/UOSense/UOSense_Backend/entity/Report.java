package UOSense.UOSense_Backend.entity;

import UOSense.UOSense_Backend.common.converter.DetailConverter;
import UOSense.UOSense_Backend.common.enumClass.Detail;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Entity
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)  // Review와 다대일 관계
    @JoinColumn(name = "review_id", nullable = false)  // 외래키 명시
    private Review review;

    @ManyToOne(fetch = FetchType.LAZY)  // User와 다대일 관계
    @JoinColumn(name = "user_id", nullable = false)  // 외래키 명시
    private User user;

    @Convert(converter = DetailConverter.class)
    @Column(name = "details")
    private Detail detail;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
