package com.example.uosense.fragments

import TokenManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.uosense.R
import com.example.uosense.databinding.FragmentBasicInfoBinding
import com.example.uosense.models.RestaurantBasicInfo
import com.example.uosense.network.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BasicInfoFragment : Fragment() {

    private var _binding: FragmentBasicInfoBinding? = null
    private val binding get() = _binding!!
    private lateinit var tokenManager: TokenManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBasicInfoBinding.inflate(inflater, container, false)

        // 토큰 매니저 초기화
        tokenManager = TokenManager(requireContext())

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // 기본 정보 수집 메서드
    private fun collectBasicInfo(): RestaurantBasicInfo {
        val restaurantName = binding.root.findViewById<EditText>(R.id.editTextRestaurantName).text.toString().trim()
        val restaurantAddress = binding.root.findViewById<EditText>(R.id.editTextRestaurantAddress).text.toString().trim()
        val phoneNumber = binding.root.findViewById<EditText>(R.id.editTextPhoneNumber).text.toString().trim()
        val subDescription = binding.root.findViewById<EditText>(R.id.editTextCategory).text.toString().trim()

        return RestaurantBasicInfo(
            restaurantId = 1, // 기본값 설정
            name = restaurantName,
            address = restaurantAddress,
            phoneNumber = phoneNumber,
            subDescription = subDescription.ifEmpty { "BAR" }
        )
    }

    // 서버로 기본 정보 전송 메서드
    public fun sendBasicInfoToServer() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val accessToken = tokenManager.getAccessToken().orEmpty()
                if (accessToken.isEmpty()) {
                    showToast("로그인이 필요합니다.")
                    return@launch
                }

                val basicInfo = collectBasicInfo()

                val response = RetrofitInstance.restaurantApi.createBasicInfo(
                    accessToken = "Bearer $accessToken",
                    restaurantBasicInfo = basicInfo
                )

                if (response.isSuccessful) {
                    showToast("기본 정보가 등록되었습니다.")
                } else {
                    showToast("등록 실패: ${response.code()}")
                }
            } catch (e: Exception) {
                showToast("오류 발생: ${e.message}")
            }
        }
    }

    // 표준 토스트 메시지 표시 메서드
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
