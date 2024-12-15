package UOSense.UOSense_Backend.service;

import UOSense.UOSense_Backend.dto.BookMarkResponse;
import UOSense.UOSense_Backend.entity.BookMark;
import UOSense.UOSense_Backend.entity.Restaurant;
import UOSense.UOSense_Backend.entity.User;
import UOSense.UOSense_Backend.repository.BookMarkRepository;
import UOSense.UOSense_Backend.repository.RestaurantRepository;
import UOSense.UOSense_Backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookMarkServiceImpl implements BookMarkService{
    private final BookMarkRepository bookMarkRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;

    @Override
    @Transactional
    public void register(int userId, int restaurantId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new IllegalArgumentException("사용자 정보가 없습니다.");
        }
        Optional<Restaurant> restaurant = restaurantRepository.findById(restaurantId);
        if (restaurant.isEmpty()) {
            throw new IllegalArgumentException("식당 정보가 없습니다.");
        }

        BookMark bookMark = BookMark.toEntity(user.get(), restaurant.get());

        bookMarkRepository.save(bookMark);
    }

    @Override
    @Transactional
    public void remove(int userId, int restaurantId) {
        if (!bookMarkRepository.existsByUserIdAndRestaurantId(userId, restaurantId)) {
            throw new IllegalArgumentException("삭제할 즐겨찾기가 존재하지 않습니다.");
        }
        bookMarkRepository.deleteByUserIdAndRestaurantId(userId, restaurantId);
    }

    @Override
    public List<BookMarkResponse> find(int userId) {
        List<BookMark> bookMarkList = bookMarkRepository.findAllByUserId(userId);
        if (bookMarkList.isEmpty()) {
            throw new IllegalArgumentException("북마크 정보가 없습니다.");
        }
        return bookMarkList.stream()
                .map(BookMarkResponse::from)
                .toList();
    }
}
