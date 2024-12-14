package UOSense.UOSense_Backend.service;

import UOSense.UOSense_Backend.dto.PurposeDayInfo;
import UOSense.UOSense_Backend.dto.PurposeDayList;
import UOSense.UOSense_Backend.entity.PurposeBusinessDay;
import UOSense.UOSense_Backend.entity.Restaurant;
import UOSense.UOSense_Backend.entity.User;
import UOSense.UOSense_Backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PurposeDayServiceImpl implements PurposeDayService {
    private final PurposeDayRepository purposeDayRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;

    @Override
    public PurposeDayList find(int restaurantId) {
        List<PurposeBusinessDay> purposeBusinessDayList = purposeDayRepository.findAllByRestaurantId(restaurantId);
        if (purposeBusinessDayList.isEmpty()) {
            throw new IllegalArgumentException("식당에 대한 영업 정보가 존재하지 않습니다.");
        }
        List<PurposeDayInfo> infoList = purposeBusinessDayList.stream()
                .map(PurposeDayInfo::from)
                .toList();
        return new PurposeDayList(restaurantId, infoList);
    }

    @Override
    @Transactional
    public void register(PurposeDayList purposeDayList, int userId) {
        Restaurant restaurant = restaurantRepository.findById(purposeDayList.getRestaurantId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 식당입니다."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저 정보입니다."));

        List<PurposeDayInfo> InfoList = purposeDayList.getPurposeDayInfoList();
        for(PurposeDayInfo purposeDayInfo : InfoList) {
            // breakTime이 없을 경우
            if (!purposeDayInfo.isBreakTime()) {
                purposeDayInfo.setBreakTime();
            }

            // 휴무일일 경우
            if (purposeDayInfo.isHoliday()) {
                purposeDayInfo.setTime();
            }

            PurposeBusinessDay purposeBusinessDay = PurposeDayInfo.toEntity(purposeDayInfo, restaurant, user);
            purposeDayRepository.save(purposeBusinessDay);
        }
    }

    @Override
    @Transactional
    public void delete(int purposeDayId) {
        if (!purposeDayRepository.existsById(purposeDayId)) {
            throw new IllegalArgumentException("삭제할 영업 정보가 존재하지 않습니다.");
        }
        purposeDayRepository.deleteById(purposeDayId);
    }
}
