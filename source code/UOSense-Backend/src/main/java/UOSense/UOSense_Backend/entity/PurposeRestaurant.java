package UOSense.UOSense_Backend.entity;

import UOSense.UOSense_Backend.common.converter.SubDescriptionConverter;
import UOSense.UOSense_Backend.common.enumClass.SubDescription;
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
@Table(name = "Purpose_Restaurant")
public class PurposeRestaurant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)  // Restaurant와 다대일 관계
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    private String name;

    private String address;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Convert(converter = SubDescriptionConverter.class)
    @Column(name = "sub_description")
    private SubDescription subDescription;

    @ManyToOne(fetch = FetchType.LAZY)  // User와 다대일 관계
    @JoinColumn(name = "user_id", nullable = false)  // 외래키 명시
    private User user;
}
