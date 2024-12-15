package com.example.uosense.fragments

import TokenManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.uosense.R
import com.example.uosense.databinding.FragmentBusinessHoursBinding
import com.example.uosense.models.BusinessDayRequest
import com.example.uosense.models.PurposeDayInfo
import com.example.uosense.network.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class BusinessHoursFragment : Fragment() {

    private var _binding: FragmentBusinessHoursBinding? = null
    private val binding get() = _binding!!

    private val daysOfWeek = listOf("월요일", "화요일", "수요일", "목요일", "금요일", "토요일", "일요일")
    private var currentRestaurantId: Int = 1
    private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    private lateinit var tokenManager: TokenManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBusinessHoursBinding.inflate(inflater, container, false)
        tokenManager = TokenManager(requireContext())

        // 영업 시간 입력 필드 추가
        addBusinessHoursRows()

        // 브레이크 타임 입력 필드 추가 버튼 리스너
        binding.addInfoBtn.setOnClickListener { addCustomInfoRow() }


        return binding.root
    }

    // 서버로 영업 시간 정보 전송 메서드
    public fun sendBusinessHoursToServer() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val accessToken = tokenManager.getAccessToken().orEmpty()
                if (accessToken.isEmpty()) {
                    showToast("로그인이 필요합니다.")
                    return@launch
                }

                // 서버로 전송할 영업 정보 수집
                val businessDayInfoList = collectBusinessDayInfo()

                val request = BusinessDayRequest(
                    restaurantId = currentRestaurantId,
                    purposeDayInfoList = businessDayInfoList
                )

                // 각 시간 항목 로깅
                businessDayInfoList.forEach { dayInfo ->
                    Log.d(
                        "BusinessDayRequest",
                        "요일: ${dayInfo.dayOfWeek}, 영업 시작: ${dayInfo.openingTime}, 영업 종료: ${dayInfo.closingTime}, " +
                                "브레이크 시작: ${dayInfo.startBreakTime}, 브레이크 종료: ${dayInfo.stopBreakTime}, 휴일 여부: ${dayInfo.holiday}"
                    )
                }

                Log.d("BusinessDayRequest", "요청 본문: $request")

                // 서버 API 호출
                val response = RetrofitInstance.restaurantApi.createBusinessDay(
                    accessToken = "Bearer $accessToken",
                    businessDayRequest = request
                )

                if (response.isSuccessful) {
                    showToast("영업 시간이 등록되었습니다.")
                } else {
                    showToast("등록 실패: ${response.code()}")
                }
            } catch (e: Exception) {
                showToast("오류 발생: ${e.message}")
            }
        }
    }

    // 사용자가 입력한 영업 정보 수집 메서드
    private fun collectBusinessDayInfo(): List<PurposeDayInfo> {
        val businessDayInfoList = mutableListOf<PurposeDayInfo>()

        // 기본 영업 시간 수집
        for (i in 0 until binding.businessHoursContainer.childCount) {
            val row = binding.businessHoursContainer.getChildAt(i) as LinearLayout
            val dayTextView = row.findViewById<TextView>(R.id.dayTextView)
            val openingTime = forceTimeFormat(row.findViewById<EditText>(R.id.openingTimeInput).text.toString())
            val closingTime = forceTimeFormat(row.findViewById<EditText>(R.id.closingTimeInput).text.toString())
            val holidayCheckBox = row.findViewById<CheckBox>(R.id.holidayCheckBox)

            businessDayInfoList.add(
                PurposeDayInfo(
                    dayOfWeek = getEnglishDayOfWeek(dayTextView.text.toString()),
                    openingTime = openingTime,
                    closingTime = closingTime,
                    holiday = holidayCheckBox.isChecked,
                    breakTime = false,
                    startBreakTime = "00:00:00",
                    stopBreakTime = "00:00:00"
                )
            )
        }

        // 브레이크 타임 수집
        for (i in 0 until binding.customInfoContainer.childCount) {
            val row = binding.customInfoContainer.getChildAt(i) as LinearLayout
            val daySpinner = row.findViewById<Spinner>(R.id.daySpinner)
            val breakStartTime = forceTimeFormat(row.findViewById<EditText>(R.id.breakStartInput).text.toString())
            val breakStopTime = forceTimeFormat(row.findViewById<EditText>(R.id.breakStopInput).text.toString())

            val selectedDay = daySpinner.selectedItem?.toString() ?: ""

            if (selectedDay.isNotEmpty()) {
                businessDayInfoList.add(
                    PurposeDayInfo(
                        dayOfWeek = getEnglishDayOfWeek(selectedDay),
                        openingTime = "00:00:00",
                        closingTime = "00:00:00",
                        holiday = false,
                        breakTime = true,
                        startBreakTime = breakStartTime,
                        stopBreakTime = breakStopTime
                    )
                )
            }
        }
        return businessDayInfoList
    }


    // 시간 형식을 강제로 변경하는 메서드
    private fun forceTimeFormat(time: String): String {
        return when {
            time.matches(Regex("\\d{2}:\\d{2}")) -> "$time:00" // HH:mm 형식 -> HH:mm:00
            time.matches(Regex("\\d{2}:\\d{2}:\\d{2}")) -> time // 이미 올바른 HH:mm:ss 형식
            else -> "00:00:00" // 유효하지 않으면 기본값
        }
    }

    // 시간을 변환하고 필수 형식을 검증하는 메서드
    private fun parseLocalTime(inputText: String): String {
        val cleanedInput = inputText.trim()

        return try {
            // 유효성 검사 및 형식 지정
            val timeText = when {
                cleanedInput.matches(Regex("^\\d{2}:\\d{2}:\\d{2}$")) -> cleanedInput
                cleanedInput.matches(Regex("^\\d{2}:\\d{2}$")) -> "$cleanedInput:00"
                else -> throw DateTimeParseException("잘못된 시간 형식", cleanedInput, 0)
            }

            Log.d("TimeParsing", "변환된 시간: $timeText")
            LocalTime.parse(timeText, timeFormatter).toString()
        } catch (e: DateTimeParseException) {
            Log.e("TimeParsing", "시간 변환 오류: ${e.message}, 기본값 사용")
            "00:00:00"
        }
    }

    // 요일을 영어로 변환하는 메서드
    private fun getEnglishDayOfWeek(koreanDay: String): String {
        return when (koreanDay) {
            "월요일" -> "Monday"
            "화요일" -> "Tuesday"
            "수요일" -> "Wednesday"
            "목요일" -> "Thursday"
            "금요일" -> "Friday"
            "토요일" -> "Saturday"
            "일요일" -> "Sunday"
            else -> "Unknown"
        }
    }

    // 기본 영업 시간 필드 추가
    private fun addBusinessHoursRows() {
        for (day in daysOfWeek) {
            val row = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_business_hours_row, binding.businessHoursContainer, false)
            row.findViewById<TextView>(R.id.dayTextView).text = day
            binding.businessHoursContainer.addView(row)
        }
    }

    // 브레이크 타임 추가 필드 생성
    private fun addCustomInfoRow() {
        val row = LayoutInflater.from(requireContext())
            .inflate(R.layout.item_custom_info_row, binding.customInfoContainer, false)

        val daySpinner = row.findViewById<Spinner>(R.id.daySpinner)

        // 어댑터 설정
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            daysOfWeek
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        daySpinner.adapter = adapter

        // 초기 선택 값 로깅
        daySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                Log.d("DaySpinner", "선택된 요일: ${daysOfWeek[position]}")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                Log.d("DaySpinner", "아무 것도 선택되지 않음")
            }
        }
        binding.customInfoContainer.addView(row)
    }

    // 메시지 출력 메서드
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
