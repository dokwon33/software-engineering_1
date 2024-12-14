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
@Table(name = "Review_Image")
public class ReviewImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)  // Review와 다대일 관계
    @JoinColumn(name = "review_id")  // 외래키 명시
    private Review review;

    @Column(name = "image_url")
    private String imageUrl;
}
