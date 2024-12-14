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
@Table(name = "Purpose_Restaurant_Image")
public class PurposeRestImg {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)  // Restaurant와 다대일 관계
    @JoinColumn(name = "restaurant_id", nullable = false)  // 외래키 명시
    private PurposeRestaurant purposeRestaurant;

    @Column(name = "image_url")
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)  // User와 다대일 관계
    @JoinColumn(name = "user_id", nullable = false)  // 외래키 명시
    private User user;
}
