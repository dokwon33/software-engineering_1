package com.example.uosense

import TokenManager
import android.content.ContentResolver
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import androidx.core.content.ContextCompat
import android.util.Log
import com.example.uosense.models.RestaurantInfo
import com.example.uosense.models.ReviewRequest
import com.example.uosense.network.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.time.LocalDateTime

/**
 * **ReviewWriteActivity**
 *
 * 사용자가 리뷰를 작성하는 액티비티입니다.
 * 리뷰 작성 기능을 제공합니다.
 */

class ReviewWriteActivity : AppCompatActivity() {


    /** 후기 입력 관련 */
    private lateinit var tokenManager: TokenManager
    private lateinit var reviewInput: EditText
    private lateinit var reviewCharacterCount: TextView

    /** 사진 추가 관련 */
    private lateinit var photoAttachmentLayout: LinearLayout
    private val PICK_IMAGE_REQUEST = 1

    /** 별점 저장 변수 */
    private lateinit var myRatingBar: RatingBar
    private var currentRating: Float = 0f

    /** 특징 선택 상태 변수 */
    private var selectedTag: String? = null

    /** 리뷰 이벤트 체크박스 */
    private lateinit var reviewEventCheckBox: CheckBox
    private var reviewEventCheck = false

    /** 선택된 이미지 URI 관리 */
    private val selectedImageUris: MutableList<Uri> = mutableListOf()

    private var restaurantId: Int = 1

    /** 등록 버튼 */
    private lateinit var registerReviewBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review_write)

        /** 식당 ID 수신 */
        restaurantId = intent.getIntExtra("restaurantId", 1)

        /** TokenManager 초기화 */
        tokenManager = TokenManager(this)

        /** 초기화 */
        reviewInput = findViewById(R.id.reviewInput)
        reviewCharacterCount = findViewById(R.id.reviewCharacterCount)
        photoAttachmentLayout = findViewById(R.id.photoAttachmentLayout)
        myRatingBar = findViewById(R.id.myRatingBar)
        reviewEventCheckBox = findViewById(R.id.eventCheckBox)
        registerReviewBtn = findViewById(R.id.registerReviewBtn)

        /** 사진 추가 버튼 설정 */
        val photoPlusBtn: Button = findViewById(R.id.photoPlusBtn)
        photoPlusBtn.setOnClickListener { openImagePicker() }

        /** 리뷰 이벤트 체크박스 상태 업데이트 */
        reviewEventCheckBox.setOnCheckedChangeListener { _, isChecked ->
            reviewEventCheck = isChecked
        }

        /** 별점 조정 */
        myRatingBar.setIsIndicator(false)
        myRatingBar.setOnRatingBarChangeListener { _, rating, _ ->
            currentRating = rating
            Toast.makeText(this, "현재 별점: $currentRating", Toast.LENGTH_SHORT).show()
        }

        /** 후기 입력 시 글자 수 업데이트 */
        reviewInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val currentLength = s?.length ?: 0
                reviewCharacterCount.text = "$currentLength/200"
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        /** 특징 버튼 설정 */
        setupFeatureButton(R.id.serviceBtn, "GOOD_SERVICE", R.color.green)
        setupFeatureButton(R.id.dateRecommendBtn, "DATE_PLACE", R.color.pink)
        setupFeatureButton(R.id.soloEatBtn, "SOLO_POSSIBLE", R.color.teal_700)
        setupFeatureButton(R.id.kindOwnerBtn, "KIND_BOSS", R.color.purple_200)
        setupFeatureButton(R.id.interiorBtn, "NICE_INTERIOR", R.color.orange)

        /** 등록 버튼 클릭 리스너 설정 */
        registerReviewBtn.setOnClickListener { submitReview() }

        loadRestaurantData()
    }

    /** 식당 정보를 API로부터 불러옵니다. */
    private fun loadRestaurantData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                /** 액세스 토큰 유효성 확인 및 새로 고침 */
                val accessToken = tokenManager.ensureValidAccessToken()

                if (accessToken.isNullOrEmpty()) {
                    Toast.makeText(this@ReviewWriteActivity, "로그인이 필요합니다.", Toast.LENGTH_SHORT)
                        .show()
                    return@launch
                }
                val response = RetrofitInstance.restaurantApi.getRestaurantById(restaurantId)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        bindRestaurantData(response.body()!!)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /** 식당 데이터를 UI에 바인딩합니다. */
    private fun bindRestaurantData(restaurant: RestaurantInfo) {
        findViewById<TextView>(R.id.restaurantName).text = restaurant.name
    }

    /** 사진 선택기 열기 */
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    /** 이미지 추가 기능 */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data?.data != null) {
            val imageUri = data.data
            if (imageUri != null) {
                selectedImageUris.add(imageUri)
                addImageToLayout(imageUri)
            }
        }
    }

    private fun addImageToLayout(imageUri: Uri) {
        val imageView = ImageView(this)
        imageView.setImageURI(imageUri)
        photoAttachmentLayout.addView(imageView)
    }

    /** 특징 버튼 설정 함수 */
    private fun setupFeatureButton(buttonId: Int, tag: String, colorId: Int) {
        val button: Button = findViewById(buttonId)
        val defaultBackground: Drawable = button.background

        button.setOnClickListener {
            if (selectedTag == tag) {
                selectedTag = null
                button.background = defaultBackground
            } else {
                resetFeatureButtons()
                selectedTag = tag
                button.setBackgroundColor(ContextCompat.getColor(this, colorId))
            }
        }
    }

    /** 모든 버튼 상태 초기화 함수 */
    private fun resetFeatureButtons() {
        val buttonBackgrounds = mapOf(
            R.id.serviceBtn to R.drawable.rounded_border_green,
            R.id.dateRecommendBtn to R.drawable.rounded_border_pink,
            R.id.soloEatBtn to R.drawable.rounded_border_tealed_700,
            R.id.kindOwnerBtn to R.drawable.rounded_border_purple_200,
            R.id.interiorBtn to R.drawable.rounded_border_orange
        )

        buttonBackgrounds.forEach { (id, backgroundRes) ->
            val button: Button = findViewById(id)
            button.background = ContextCompat.getDrawable(this, backgroundRes)
        }
    }

    /**    이미지 파일 변환 */
    private fun prepareFilePart(partName: String, fileUri: Uri): MultipartBody.Part {
        val fileDescriptor = contentResolver.openFileDescriptor(fileUri, "r")
            ?: return MultipartBody.Part.createFormData(partName, "")
        val inputStream = contentResolver.openInputStream(fileUri)
        val file = File(cacheDir, contentResolver.getFileName(fileUri)) // 캐시에 저장

        file.outputStream().use { output ->
            inputStream?.copyTo(output)
        }

        /** 로그: 파일 경로 및 크기 확인 */
        Log.d("FilePart", "File Path: ${file.absolutePath}, File Size: ${file.length()} bytes")

        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        Log.d("RequestFile", "Request File Size: ${requestFile.contentLength()} bytes")
        return MultipartBody.Part.createFormData(partName, file.name, requestFile)
    }

    /** 파일 이름 가져오기 함수 추가 */
    private fun ContentResolver.getFileName(uri: Uri): String {
        var name = "temp_file"
        val cursor = query(uri, null, null, null, null)
        if (cursor != null) {
            val nameIndex = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
            if (nameIndex != -1) {
                cursor.moveToFirst()
                name = cursor.getString(nameIndex)
            }
            cursor.close()
        }
        return name
    }


    /**    리뷰 이미지 업로드 함수 */
    private fun uploadReviewImages(reviewId: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                /** 선택된 이미지 URI를 MultipartBody.Part로 변환 */
                val imageParts = selectedImageUris.map { uri ->
                    prepareFilePart("images", uri)
                }

                /** 로그: 변환된 이미지 데이터와 리뷰 ID 확인 */
                Log.d("ImageUpload", "Review ID: $reviewId")
                imageParts.forEachIndexed { index, part ->
                    Log.d(
                        "ImageUpload",
                        "Image Part [$index]: ${part.body.contentType()} - ${part.headers}"
                    )
                }

                /** API 호출 전 */
                Log.d(
                    "ImageUpload",
                    "Requesting upload for Review ID: $reviewId with ${imageParts.size} parts"
                )

                /** API 호출 */
                val response =
                    RetrofitInstance.restaurantApi.uploadReviewImages(reviewId, imageParts)
                if (response.isSuccessful) {
                    Log.d(
                        "ImageUpload",
                        "Image upload successful. Response code: ${response.code()}"
                    )
                    Toast.makeText(this@ReviewWriteActivity, "이미지 업로드 성공!", Toast.LENGTH_SHORT)
                        .show()
                    finish()

                } else {
                    Log.e(
                        "ImageUpload",
                        "이미지 업로드 실패: ${response.code()} - ${response.errorBody()?.string()}"
                    )
                    Toast.makeText(this@ReviewWriteActivity, "이미지 업로드 실패", Toast.LENGTH_SHORT)
                        .show()
                    finish()
                }
            } catch (e: Exception) {
                Log.e("ImageUpload", "오류 발생: ${e.message}")
                Toast.makeText(this@ReviewWriteActivity, "이미지 업로드 중 오류 발생", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }


    /** 리뷰 등록 API 호출 */
    private fun submitReview() {
        val reviewBody = reviewInput.text.toString()
        val ratingValue = currentRating
        val dateTime = LocalDateTime.now().toString()

        /** 입력 검증 */
        if (reviewBody.isBlank()) {
            Toast.makeText(this, "리뷰 내용을 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        if (ratingValue == 0f) {
            Toast.makeText(this, "별점을 선택해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        /** 로그 메시지 출력 (전송 데이터 확인) */

        /** API 호출 */
        CoroutineScope(Dispatchers.Main).launch {

            /** 액세스 토큰 유효성 확인 및 새로 고침 */
            val accessToken = tokenManager.ensureValidAccessToken()

            if (accessToken.isNullOrEmpty()) {
                /** 로그인 화면으로 이동 */
                Toast.makeText(this@ReviewWriteActivity, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val reviewRequest = ReviewRequest(
                restaurantId = restaurantId,
                body = reviewBody,
                rating = ratingValue.toDouble(),
                dateTime = dateTime,
                reviewEventCheck = reviewEventCheck,
                tag = selectedTag
                /** 선택 사항 */
            )

            try {
                val response = RetrofitInstance.restaurantApi.createReview(
                    reviewRequest,
                    "Bearer $accessToken"
                    /** Bearer 포함 */
                )

                /** 응답 처리 */
                if (response.isSuccessful) {
                    val reviewId = response.body()
                    /** 서버가 반환한 리뷰 ID 가져오기 */
                    if (reviewId != null) {
                        Toast.makeText(this@ReviewWriteActivity, "리뷰가 등록되었습니다!", Toast.LENGTH_SHORT)
                            .show()
                        uploadReviewImages(reviewId)
                        /** 이미지 업로드 호출 */
                    } else {
                        Toast.makeText(
                            this@ReviewWriteActivity,
                            "리뷰 ID를 가져오지 못했습니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Log.e(
                        "SubmitReview",
                        "Response Failed: Code=${response.code()}, Message=${
                            response.errorBody()?.string()
                        }"
                    )
                    Toast.makeText(
                        this@ReviewWriteActivity,
                        "등록 실패: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e("SubmitReview", "Server Error: ${e.message}")
                Toast.makeText(
                    this@ReviewWriteActivity,
                    "서버 오류 발생: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
