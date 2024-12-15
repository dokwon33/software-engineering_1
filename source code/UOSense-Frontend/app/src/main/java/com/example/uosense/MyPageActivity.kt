package com.example.uosense

import TokenManager
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.uosense.network.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

/**
 * **MyPageActivity**
 *
 * 마이 페이지 정보를 표시하는 액티비티입니다.
 * 내 닉네임 확인, 내 즐겨찾기 목록, 내 리뷰 목록 조회 기능을 제공합니다.
 */


class MyPageActivity : AppCompatActivity() {

    /** UI 요소 선언 */
    private lateinit var logOutBtn: Button
    private lateinit var backBtn: Button
    private lateinit var updateBtn: Button
    private lateinit var uploadImageBtn: Button
    private lateinit var favoriteDetailsBtn: Button
    private lateinit var reviewDetailsBtn: Button
    private lateinit var nicknameText: EditText
    private lateinit var profileImage: ImageView
    private lateinit var tokenManager: TokenManager

    private var uploadedImageUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_page)

        /** 토큰 관리 객체 초기화 */
        tokenManager = TokenManager(this)

        /** 로그인 여부 확인 */
        if (tokenManager.getRefreshToken().isNullOrEmpty()) {
            showToast("로그인이 필요합니다.")
            navigateToLoginActivity()
            return
        }

        /** UI 요소 초기화 */
        initializeUIElements()

        /** 사용자 프로필 정보 가져오기 */
        fetchUserProfile()

        /** 버튼 클릭 리스너 설정 */
        logOutBtn.setOnClickListener { logOutUser() }
        backBtn.setOnClickListener { navigateToMainActivity() }
        updateBtn.setOnClickListener { updateUserProfile() }
        uploadImageBtn.setOnClickListener { pickImageFromGallery() }

        favoriteDetailsBtn.setOnClickListener {
            startActivity(Intent(this, FavoriteListActivity::class.java))
        }

        reviewDetailsBtn.setOnClickListener {
            startActivity(Intent(this, MyReviewListActivity::class.java))
        }

        /** 뒤로 가기 버튼 동작 설정 */
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateToMainActivity()
            }
        })
    }

    /**
     * UI 요소 초기화 메서드
     */
    private fun initializeUIElements() {
        logOutBtn = findViewById(R.id.logOutBtn)
        backBtn = findViewById(R.id.backBtn)
        updateBtn = findViewById(R.id.editProfile)
        uploadImageBtn = findViewById(R.id.uploadImageBtn)
        favoriteDetailsBtn = findViewById(R.id.favoriteDetailsBtn)
        reviewDetailsBtn = findViewById(R.id.reviewDetailsBtn)
        nicknameText = findViewById(R.id.userName)
        profileImage = findViewById(R.id.profileImage)
    }

    /**
     * 사용자 프로필 정보 가져오기
     */
    private fun fetchUserProfile() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val accessToken = tokenManager.ensureValidAccessToken() ?: run {
                    showToast("로그인이 필요합니다.")
                    navigateToLoginActivity()
                    return@launch
                }

                val response = RetrofitInstance.restaurantApi.getUserProfile("Bearer $accessToken")
                nicknameText.setText(response.nickname)
                uploadedImageUrl = response.imageUrl

                Glide.with(this@MyPageActivity)
                    .load(response.imageUrl)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .into(profileImage)

                profileImage.tag = response.imageUrl
                nicknameText.hint = response.nickname

            } catch (e: Exception) {
                showToast("프로필 조회 실패: ${e.message}")
            }
        }
    }

    /**
     * 프로필 업데이트 메서드
     */
    private fun updateUserProfile() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val accessToken = tokenManager.ensureValidAccessToken() ?: run {
                    showToast("로그인이 필요합니다.")
                    navigateToLoginActivity()
                    return@launch
                }

                val newNickname = nicknameText.text.toString().takeIf { it.isNotBlank() }
                val currentNickname = nicknameText.hint.toString()
                val currentImageUrl = profileImage.tag as? String

                if (newNickname == currentNickname && uploadedImageUrl == currentImageUrl) {
                    showToast("변경된 내용이 없습니다.")
                    return@launch
                }

                val imagePart = when {
                    uploadedImageUrl == null -> MultipartBody.Part.createFormData("image", "")
                    uploadedImageUrl != currentImageUrl -> createMultipartBodyFromUri(Uri.parse(uploadedImageUrl))
                    else -> null
                }

                val response = RetrofitInstance.restaurantApi.updateUserProfile(
                    accessToken = "Bearer $accessToken",
                    nickname = if (newNickname != currentNickname) newNickname else null,
                    image = imagePart
                )

                if (response.isSuccessful) {
                    showToast("프로필이 성공적으로 업데이트되었습니다.")
                    fetchUserProfile()
                } else {
                    val errorBody = response.errorBody()?.string().orEmpty()
                    showToast("업데이트 실패: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                showToast("오류 발생: ${e.message}")
            }
        }
    }

    /**
     * 갤러리에서 이미지 선택
     */
    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
        startActivityForResult(intent, 1000)
    }

    /**
     * 선택된 이미지 처리
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1000 && resultCode == RESULT_OK) {
            val imageUri = data?.data
            uploadedImageUrl = imageUri.toString()

            Glide.with(this)
                .load(imageUri)
                .placeholder(R.drawable.ic_profile_placeholder)
                .into(profileImage)

            profileImage.tag = uploadedImageUrl
        }
    }

    /**
     * 메인 화면으로 이동
     */
    private fun navigateToMainActivity() {
        val intent = Intent(this, RestaurantDetailActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    /**
     * 로그인 화면으로 이동
     */
    private fun navigateToLoginActivity() {
        val intent = Intent(this, StartActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    /**
     * 로그아웃 처리
     */
    private fun logOutUser() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val refreshToken = tokenManager.getRefreshToken() ?: run {
                    showToast("로그인 정보가 없습니다.")
                    navigateToLoginActivity()
                    return@launch
                }

                val response = RetrofitInstance.restaurantApi.logoutUser("refresh=$refreshToken")
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        showToast("로그아웃 성공!")
                        tokenManager.clearTokens()
                        navigateToLoginActivity()
                    } else {
                        showToast("로그아웃 실패: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("서버 오류 발생: ${e.message}")
                }
            }
        }
    }

    /**
     * 메시지 출력 메서드
     */
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }



    /**
     * URI에서 MultipartBody 생성
     */
    private fun createMultipartBodyFromUri(uri: Uri?): MultipartBody.Part? {
        if (uri == null) return null

        return try {
            val fileDescriptor = contentResolver.openFileDescriptor(uri, "r")
                ?: return MultipartBody.Part.createFormData("image", "")

            val inputStream = contentResolver.openInputStream(uri)
            val file = File(cacheDir, contentResolver.getFileName(uri))

            file.outputStream().use { output ->
                inputStream?.copyTo(output)
            }

            Log.d("FilePart", "파일 생성: ${file.absolutePath}, 크기: ${file.length()} 바이트")

            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("image", file.name, requestFile)
        } catch (e: Exception) {
            Log.e("MultipartError", "파일 생성 실패: ${e.message}")
            null
        }
    }

    /**
     * URI에서 파일 이름 가져오기
     */
    private fun ContentResolver.getFileName(uri: Uri): String {
        var name = "temp_file"
        val cursor = query(uri, null, null, null, null)
        cursor?.use {
            val nameIndex = it.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
            if (nameIndex != -1) {
                it.moveToFirst()
                name = it.getString(nameIndex)
            }
        }
        return name
    }

}
