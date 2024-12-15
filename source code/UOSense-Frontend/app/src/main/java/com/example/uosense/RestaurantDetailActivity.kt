package com.example.uosense

import BusinessDayAdapter
import MenuAdapter
import MenuImagePicker
import TokenManager
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

import com.example.uosense.databinding.ActivityControlMainBinding
import com.example.uosense.databinding.ActivityRestaurantDetailBinding
import com.example.uosense.databinding.ActivitySelectedDoorBinding
import com.example.uosense.models.BusinessDayInfo
import com.example.uosense.models.MenuResponse
import com.example.uosense.models.RestaurantInfo
import com.example.uosense.network.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
/**
 * **RestaurantDetailActivity**
 *
 * 식당 세부 정보를 표시하는 액티비티입니다.
 * 식당 정보, 영업일, 메뉴, 리뷰 목록 및 즐겨찾기 기능을 제공합니다.
 */
class RestaurantDetailActivity : AppCompatActivity(), MenuImagePicker {

    private lateinit var recyclerView: RecyclerView
    private lateinit var businessDaysBtn: Button
    private lateinit var menuBtn: Button
    private lateinit var reviewBtn: Button
    private lateinit var backBtn: Button
    private lateinit var favoriteButton: ImageButton
    private lateinit var reviewOptionsLayout: LinearLayout
    private lateinit var reviewListBtn: Button
    private lateinit var reviewWriteBtn: Button
    // 정보 수정 제안 버튼
    private lateinit var restaurantUpdateBtn : Button

    // 선택된 메뉴 위치를 저장하는 변수 추가
    private var selectedMenuPosition: Int = -1
    private lateinit var restaurantImage: ImageView

    private lateinit var businessDayAdapter: BusinessDayAdapter
    private lateinit var menuAdapter: MenuAdapter

    private var isFavorite = false
    private var restaurantId: Int = 1

    private lateinit var tokenManager : TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_detail)
        tokenManager = TokenManager(this)
        // UI 초기화
        recyclerView = findViewById(R.id.businessDaysRecyclerView)
        businessDaysBtn = findViewById(R.id.businessDaysBtn)
        menuBtn = findViewById(R.id.menuBtn)
        reviewBtn = findViewById(R.id.reviewBtn)
        backBtn = findViewById(R.id.backBtn)
        favoriteButton = findViewById(R.id.favoriteButton)
        reviewOptionsLayout = findViewById(R.id.reviewOptionsLayout)
        reviewListBtn = findViewById(R.id.reviewListBtn)
        reviewWriteBtn = findViewById(R.id.reviewWriteBtn)
        restaurantUpdateBtn = findViewById(R.id.restaurantUpdateBtn)

        restaurantImage = findViewById(R.id.restaurantImage)

        // 리사이클러 뷰 설정
        recyclerView.layoutManager = LinearLayoutManager(this)
        businessDayAdapter = BusinessDayAdapter(BusinessDayMode.DISPLAY)
        menuAdapter = MenuAdapter(MenuMode.DISPLAY, this)
        recyclerView.adapter = businessDayAdapter

        // 버튼 클릭 리스너 설정
        businessDaysBtn.setOnClickListener { showBusinessDays() }
        menuBtn.setOnClickListener { showMenuItems() }
        reviewBtn.setOnClickListener { showReviewOptions() }

        restaurantUpdateBtn.setOnClickListener {
            startActivity(Intent(this, RestaurantInfoSuggestionActivity::class.java).apply {
                putExtra("restaurantId", restaurantId)
            })
        }


        reviewListBtn.setOnClickListener {
            startActivity(Intent(this, ReviewListActivity::class.java).apply {
                putExtra("restaurantId", restaurantId)
            })
        }

        reviewWriteBtn.setOnClickListener {
            startActivity(Intent(this, ReviewWriteActivity::class.java).apply {
                putExtra("restaurantId", restaurantId)
            })
        }

        // 뒤로 가기 버튼 클릭
        backBtn.setOnClickListener { finish() }

        // 물리적 뒤로 가기 활성화
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })

        // 즐겨찾기 버튼 클릭
        favoriteButton.setOnClickListener { toggleFavorite() }

        // 식당 ID 수신
        restaurantId = intent.getIntExtra("restaurantId", 1)

        // 식당 정보 없을 시 ! (절대 발생하면 안됨)
        if (restaurantId == -1) {
            finish()
            return
        }

        // 데이터 로드
        loadRestaurantData()
        loadBusinessDays()
        loadRestaurantImages()
    }
    // MenuImagePicker 인터페이스 구현
    override fun openImagePicker(position: Int) {
        selectedMenuPosition = position // 선택된 메뉴 위치 저장
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_IMAGE_PICKER)
    }

    // onActivityResult에서 선택된 위치 사용
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICKER && resultCode == RESULT_OK && selectedMenuPosition != -1) {
            val selectedImageUri = data?.data ?: return
            menuAdapter.updateMenuImage(selectedMenuPosition, selectedImageUri)
        }
    }
    companion object {
        const val REQUEST_IMAGE_PICKER = 1001
    }


    /**
     * 식당 정보를 API로부터 불러옵니다.
     */
    private fun loadRestaurantData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 액세스 토큰 유효성 확인 및 새로 고침
                val accessToken = tokenManager.ensureValidAccessToken()

                if (accessToken.isNullOrEmpty()) {
                    // 로그인 화면으로 이동
                    Toast.makeText(this@RestaurantDetailActivity, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
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

    /**
     * 식당 데이터를 UI에 바인딩합니다.
     * @param restaurant API 응답에서 가져온 식당 데이터
     */
    private fun bindRestaurantData(restaurant: RestaurantInfo) {
        findViewById<TextView>(R.id.restaurantName).text = restaurant.name
        findViewById<TextView>(R.id.restaurantCategory).text = restaurant.category ?: "정보 없음"
        findViewById<TextView>(R.id.restaurantDescription).text = restaurant.description ?: "정보 없음"
        findViewById<TextView>(R.id.restaurantAddress).text = "주소: ${restaurant.address}"
        findViewById<TextView>(R.id.restaurantPhoneNumber).text = "전화: ${restaurant.phoneNumber ?: "정보 없음"}"
        findViewById<TextView>(R.id.restaurantRating).text = "평점: ${restaurant.rating ?: "정보 없음"}"
        findViewById<TextView>(R.id.restaurantReview).text = restaurant.reviewCount?.toString() ?: "0"

        isFavorite = restaurant.bookmarkId != null
        updateFavoriteIcon()
    }

    /**
     * 영업일 정보를 불러옵니다.
     */
    private fun loadBusinessDays() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 액세스 토큰 유효성 확인 및 새로 고침
                val accessToken = tokenManager.ensureValidAccessToken()

                if (accessToken.isNullOrEmpty()) {
                    // 로그인 화면으로 이동
                    Toast.makeText(this@RestaurantDetailActivity, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                val response = RetrofitInstance.restaurantApi.getBusinessDayList(restaurantId)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        businessDayAdapter.submitList(response.body()!!.businessDayInfoList)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 메뉴 정보를 불러옵니다.
     */
    private fun loadMenuItems() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 액세스 토큰 유효성 확인 및 새로 고침
                val accessToken = tokenManager.ensureValidAccessToken()

                if (accessToken.isNullOrEmpty()) {
                    // 로그인 화면으로 이동
                    Toast.makeText(this@RestaurantDetailActivity, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                val response = RetrofitInstance.restaurantApi.getMenu(restaurantId)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        menuAdapter.submitList(response.body()!!)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    /**
     * 식당 이미지를 불러옵니다.
     */
    private fun loadRestaurantImages() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 액세스 토큰 유효성 확인 및 새로 고침
                val accessToken = tokenManager.ensureValidAccessToken()

                if (accessToken.isNullOrEmpty()) {
                    // 로그인 화면으로 이동
                    Toast.makeText(this@RestaurantDetailActivity, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                val response = RetrofitInstance.restaurantApi.getRestaurantImages(restaurantId)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val imageUrl = response.body()?.imageList?.firstOrNull()?.imageUrl

                        if (!imageUrl.isNullOrEmpty()) {
                            Log.d("IMAGE_URL", "Loaded URL: $imageUrl")
                            Glide.with(this@RestaurantDetailActivity)
                                .load(imageUrl)
                                .placeholder(R.drawable.placeholder_image)
                                .error(R.drawable.ic_uos)
                                .into(restaurantImage)
                        } else {
                            Log.e("IMAGE_ERROR", "이미지가 비었습니다.")
                        }
                    } else {
                        Log.e("IMAGE_ERROR", "Response failed: ${response.errorBody()?.string()}")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("IMAGE_EXCEPTION", "Exception: ${e.message}")
            }
        }
    }

    /**
     * 영업일을 표시합니다.
     */
    private fun showBusinessDays() {
        recyclerView.adapter = businessDayAdapter
        reviewOptionsLayout.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
        loadBusinessDays() // 실제 영업일 데이터를 로드
    }


    /**
     * 즐겨찾기 상태를 토글하고 API 호출을 수행합니다.
     */
    private fun toggleFavorite() {
        if (!isFavorite) {
            addBookmark() // 즐겨찾기 추가
        } else {
            deleteBookmark() // 즐겨찾기 제거 (예시로 추가)
        }
    }
    /**
     * 즐겨찾기 추가 API 호출
     */
    private fun addBookmark() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 액세스 토큰 유효성 확인 및 새로 고침
                val accessToken = tokenManager.ensureValidAccessToken()

                if (accessToken.isNullOrEmpty()) {
                    // 로그인 화면으로 이동
                    Toast.makeText(this@RestaurantDetailActivity, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val response = RetrofitInstance.restaurantApi.addBookmark("Bearer $accessToken", restaurantId)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        isFavorite = true
                        updateFavoriteIcon()
                        Toast.makeText(this@RestaurantDetailActivity, "즐겨찾기에 추가되었습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@RestaurantDetailActivity, "즐겨찾기 추가 실패", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@RestaurantDetailActivity, "API 오류 발생: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    /**
     * 즐겨찾기 제거 API 호출
     */
    private fun deleteBookmark() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 액세스 토큰 유효성 확인 및 새로 고침
                val accessToken = tokenManager.ensureValidAccessToken()

                if (accessToken.isNullOrEmpty()) {
                    // 로그인 화면으로 이동
                    Toast.makeText(this@RestaurantDetailActivity, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // 삭제 API 호출
                val response = RetrofitInstance.restaurantApi.deleteBookmark("Bearer $accessToken", restaurantId)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        isFavorite = false
                        updateFavoriteIcon()
                        Toast.makeText(this@RestaurantDetailActivity, "즐겨찾기에서 제거되었습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@RestaurantDetailActivity, "즐겨찾기 제거 실패", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@RestaurantDetailActivity, "API 오류 발생: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * 즐겨찾기 아이콘을 업데이트합니다.
     */
    private fun updateFavoriteIcon() {
        favoriteButton.setImageResource(
            if (isFavorite) R.drawable.heart_color else R.drawable.blackheart
        )
    }

    /**
     * 리뷰 옵션(목록, 쓰기)를 보여줍니다.
     */
    private fun showReviewOptions() {
        reviewOptionsLayout.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
    }

    /**
     * 메뉴 목록을 보여줍니다.
     */
    private fun showMenuItems() {
        recyclerView.adapter = menuAdapter
        reviewOptionsLayout.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
        loadMenuItems() // 실제 메뉴 데이터 로드
    }


}
