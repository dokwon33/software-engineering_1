package UOSense.UOSense_Backend.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Review_Like", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "review_id"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ReviewLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    private Review review;

    public ReviewLike(User user, Review review) {
        this.user = user;
        this.review = review;
    }
}
