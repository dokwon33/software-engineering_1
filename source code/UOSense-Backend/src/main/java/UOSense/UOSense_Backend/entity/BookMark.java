package UOSense.UOSense_Backend.entity;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Entity
public class BookMark {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)  // Restaurant와 다대일 관계
    @JoinColumn(name = "user_id")  // 외래키 명시
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)  // User와 다대일 관계
    @JoinColumn(name = "restaurant_id")  // 외래키 명시
    private Restaurant restaurant;

    public static BookMark toEntity(User user, Restaurant restaurant) {
        return BookMark.builder()
                .user(user)
                .restaurant(restaurant)
                .build();
    }
}
