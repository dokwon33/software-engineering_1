package com.example.uosense.fragments

import TokenManager
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.uosense.R
import com.example.uosense.databinding.FragmentProductMenuBinding
import com.example.uosense.models.MenuImageRequest
import com.example.uosense.models.MenuRequest
import com.example.uosense.network.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProductMenuFragment : Fragment() {

    private var _binding: FragmentProductMenuBinding? = null
    private val binding get() = _binding!!

    private lateinit var tokenManager: TokenManager
    private var uploadedImageUrl: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductMenuBinding.inflate(inflater, container, false)
        tokenManager = TokenManager(requireContext())

        // 버튼 리스너 연결
        binding.uploadImageBtn.setOnClickListener { pickImageFromGallery() }
        binding.addCircleBtn.setOnClickListener { addPriceRow() }


        return binding.root
    }

    // 서버 전송 메서드
    public fun sendProductMenuToServer() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val accessToken = tokenManager.getAccessToken().orEmpty()
                if (accessToken.isEmpty()) {
                    Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                for (i in 0 until binding.priceContainer.childCount) {
                    val row = binding.priceContainer.getChildAt(i) as LinearLayout
                    val menuInput = row.findViewById<EditText>(R.id.menuName)
                    val priceInput = row.findViewById<EditText>(R.id.menuPrice)

                    val menuName = menuInput.text.toString()
                    val menuPrice = priceInput.text.toString().toIntOrNull() ?: 0

                    if (menuName.isNotEmpty() && menuPrice > 0) {
                        // RequestBody 생성
                        val menuImageRequest = if (uploadedImageUrl.isNullOrEmpty()) {
                            null
                        } else {
                            MenuImageRequest(image = uploadedImageUrl)
                        }

                        // API 호출
                        val response = RetrofitInstance.restaurantApi.createMenu(
                            accessToken = "Bearer $accessToken",
                            restaurantId = 1, // 예시: 임의
                            name = menuName,
                            price = menuPrice,
                            image = menuImageRequest
                        )

                        // 응답 확인
                        if (response.isSuccessful) {
                            Toast.makeText(requireContext(), "메뉴가 등록되었습니다.", Toast.LENGTH_SHORT).show()
                        } else {
                            Log.e("APIError", "등록 실패: ${response.code()} - ${response.errorBody()?.string()}")
                            Toast.makeText(
                                requireContext(),
                                "등록 실패: ${response.code()}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(requireContext(), "메뉴 정보를 입력하세요.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("APIError", "오류 발생: ${e.message}")
                Toast.makeText(requireContext(), "오류 발생: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 이미지 선택 (URI 가져오기)
    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = data?.data
            uploadedImageUrl = imageUri.toString()

            Glide.with(this)
                .load(imageUri)
                .placeholder(R.drawable.ic_profile_placeholder)
                .into(binding.imagePreview)

            binding.imagePreview.visibility = View.VISIBLE
        }
    }

    // 가격 입력 필드 추가
    private fun addPriceRow() {
        val context = requireContext()
        val priceRow = layoutInflater.inflate(R.layout.item_price_row, binding.priceContainer, false)
        binding.priceContainer.addView(priceRow)
    }

    companion object {
        private const val IMAGE_PICK_REQUEST_CODE = 1001
    }
}
