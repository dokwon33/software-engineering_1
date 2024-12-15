package UOSense.UOSense_Backend.entity;

import UOSense.UOSense_Backend.common.enumClass.Tag;
import UOSense.UOSense_Backend.common.converter.TagConverter;
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
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)  // User와 다대일 관계
    @JoinColumn(name = "restaurant_id")  // 외래키 명시
    private Restaurant restaurant;

    @ManyToOne(fetch = FetchType.LAZY)  // Restaurant와 다대일 관계
    @JoinColumn(name = "user_id")  // 외래키 명시
    private User user;

    @Column(columnDefinition = "TEXT")
    private String body;

    private double rating;

    @Column(name = "date_time")
    private LocalDateTime dateTime;

    @Column(columnDefinition = "TINYINT(1)",name = "review_event_check")
    private boolean reviewEventCheck;

    @Convert(converter = TagConverter.class)
    private Tag tag;

    @Column(name = "likes_count")
    private int likeCount;
}