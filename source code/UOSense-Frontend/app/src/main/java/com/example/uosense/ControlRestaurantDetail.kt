package com.example.uosense

import BusinessDayAdapter
import MenuAdapter
import MenuImagePicker
import TokenManager
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.uosense.databinding.ActivityControlMainBinding
import com.example.uosense.databinding.ActivityControlRestaurantDetailBinding
import com.example.uosense.databinding.ActivityRestaurantDetailBinding
import com.example.uosense.databinding.ActivitySelectedDoorBinding
import com.example.uosense.models.BusinessDayInfo
import com.example.uosense.models.BusinessDayList
import com.example.uosense.models.MenuRequest
import com.example.uosense.models.MenuResponse
import com.example.uosense.models.RestaurantInfo
import com.example.uosense.models.RestaurantRequest
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
 * **ControlRestaurantDetail**
 *
 * 식당 정보를 관리할 수 있는 관리자 전용 액티비티입니다.
 * 식당 세부 정보 수정, 영업일, 메뉴 관리 및 이미지 업로드 기능을 제공합니다.
 */
class ControlRestaurantDetail : AppCompatActivity(), MenuImagePicker {
    /**
     * 이미지를 선택하는 메서드입니다.
     * @param position 선택된 메뉴의 위치
     */
    override fun openImagePicker(position: Int) {
        currentMenuPosition = position
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }
    //위치, 경도 초기화
    private var currentLatitude: Double = 0.0
    private var currentLongitude: Double = 0.0

    // 버튼 및 리사이클러 초기화
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnEditImage: Button
    private lateinit var btnEditBusinessDays: Button
    private lateinit var btnEditMenu: Button
    private lateinit var btnSaveRestaurant: Button
    private lateinit var backBtn: Button
    private lateinit var menuRecyclerView: RecyclerView


    // TokenManager 초기화
    private lateinit var tokenManager: TokenManager

    // 사진 추가 레이아웃 초기화
    private lateinit var photoAttachmentLayout: LinearLayout

    // 선택된 이미지 URI 관리
    private val selectedImageUris: MutableList<Uri> = mutableListOf()
    private val PICK_IMAGE_REQUEST = 1
    private var currentMenuPosition: Int = -1


    private lateinit var businessDayAdapter: BusinessDayAdapter
    private lateinit var menuAdapter: MenuAdapter

    private var restaurantId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_control_restaurant_detail)

        // TokenManager 초기화
        tokenManager = TokenManager(this)

        // 각 spinner UI 변경 시 사용
        // DoorType 스피너
        val doorTypeSpinner = findViewById<Spinner>(R.id.editDoorType)
        ArrayAdapter.createFromResource(
            this,
            R.array.restaurant_door_types,
            R.layout.spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
            doorTypeSpinner.adapter = adapter
        }
        // Category 스피너
        val categorySpinner = findViewById<Spinner>(R.id.editRestaurantCategory)
        ArrayAdapter.createFromResource(
            this,
            R.array.restaurant_categories,
            R.layout.spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
            categorySpinner.adapter = adapter
        }

        // 간단 업종 스피너
        val subDescriptionSpinner = findViewById<Spinner>(R.id.editSubDescription)
        ArrayAdapter.createFromResource(
            this,
            R.array.restaurant_sub_descriptions,
            R.layout.spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
            subDescriptionSpinner.adapter = adapter
        }

        // 식당 ID 수신
        restaurantId = intent.getIntExtra("restaurantId", -1)
        if (restaurantId == -1) {
            showToast("식당 정보를 불러올 수 없습니다.")
            finish()
            return
        }
        // UI 초기화
        recyclerView = findViewById(R.id.businessDaysRecyclerView)
        btnEditBusinessDays = findViewById(R.id.btnEditBusinessDays)
        btnEditMenu = findViewById(R.id.btnEditMenu)
        btnSaveRestaurant = findViewById(R.id.btnSaveRestaurant)
        backBtn = findViewById(R.id.backBtn)
        photoAttachmentLayout = findViewById(R.id.photoAttachmentLayout)
        menuRecyclerView = findViewById(R.id.menuRecyclerView)


        // 리사이클러 뷰 설정
        recyclerView.layoutManager = LinearLayoutManager(this)
        businessDayAdapter = BusinessDayAdapter(BusinessDayMode.EDIT)
        recyclerView.adapter = businessDayAdapter


        menuAdapter = MenuAdapter(MenuMode.EDIT, object:MenuImagePicker{
            override fun openImagePicker(position: Int) {
                currentMenuPosition = position
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intent, PICK_IMAGE_REQUEST)
            }
        } )
        menuRecyclerView.layoutManager = LinearLayoutManager(this)
        menuRecyclerView.adapter = menuAdapter

        // 버튼 클릭 시 빈 항목 추가
        btnEditBusinessDays.setOnClickListener {
            businessDayAdapter.addBusinessDay(
                BusinessDayInfo(
                    id = -1,
                    dayOfWeek = AppUtils.mapDayOfWeek(""),
                    openingTime = "",
                    closingTime = "",
                    haveBreakTime = false,
                    startBreakTime = "",
                    stopBreakTime = "",
                    holiday = false
                )
            )
        }

        btnEditMenu.setOnClickListener {
            menuAdapter.addMenuItem(
                MenuResponse(
                    menuId = -1,
                    restaurantId = restaurantId,
                    name = "",
                    price = 0,
                    description = "",
                    imageUrl = null
                )
            )
        }

        btnEditImage = findViewById(R.id.photoPlusBtn)




        // 뒤로 가기 버튼 클릭
        backBtn.setOnClickListener { finish() }

        // 이미지 선택 버튼 클릭 리스너
        btnEditImage.setOnClickListener {
            openImagePicker()
        }

        // 저장 버튼 클릭 리스너
        btnSaveRestaurant.setOnClickListener {
            saveRestaurantData()
        }


        // 물리적 뒤로 가기 활성화
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })



        // 데이터 로드
        loadRestaurantData()
        loadBusinessDays()
        loadMenuItems()
        loadRestaurantImages()
    }

    /**
     * 저장 버튼이 눌러지면 식당 데이터들을 저장합니다.
     */
    private fun saveRestaurantData() {
        val name = findViewById<EditText>(R.id.editRestaurantName).text.toString()
        val description = findViewById<EditText>(R.id.editRestaurantDescription).text.toString()
        val address = findViewById<EditText>(R.id.editRestaurantAddress).text.toString()
        val phoneNumber = findViewById<EditText>(R.id.editRestaurantPhoneNumber).text.toString()

        val categoryIndex = findViewById<Spinner>(R.id.editRestaurantCategory).selectedItemPosition
        val category = resources.getStringArray(R.array.restaurant_categories)[categoryIndex]

        val subDescriptionIndex = findViewById<Spinner>(R.id.editSubDescription).selectedItemPosition
        val subDescription = resources.getStringArray(R.array.restaurant_sub_descriptions)[subDescriptionIndex]

        val doorTypeIndex = findViewById<Spinner>(R.id.editDoorType).selectedItemPosition
        val doorType = resources.getStringArray(R.array.restaurant_door_types)[doorTypeIndex]



        if (name.isBlank() || address.isBlank()) {
            showToast("이름과 주소는 필수 항목입니다.")
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            try {
                // 액세스 토큰 유효성 확인 및 새로 고침
                val accessToken = tokenManager.ensureValidAccessToken()

                if (accessToken.isNullOrEmpty()) {
                    // 로그인 화면으로 이동
                    Toast.makeText(this@ControlRestaurantDetail, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val restaurantRequest = RestaurantRequest(
                    id = restaurantId,
                    name = name,
                    doorType = AppUtils.mapDoorTypeForApi(doorType),
                    latitude = currentLatitude,
                    longitude = currentLongitude,
                    address = address,
                    phoneNumber = phoneNumber,
                    category = AppUtils.mapCategoryForApi(category),
                    subDescription = AppUtils.mapSubDescription(subDescription),
                    description = description
                )

                val restaurantResponse = RetrofitInstance.restaurantApi.editRestaurant(
                    "Bearer $accessToken",
                    restaurantRequest
                )

                if (!restaurantResponse.isSuccessful) {
                    showToast("식당 정보 저장 실패: ${restaurantResponse.errorBody()?.string()}")
                    return@launch
                }

                val businessDaySuccess = updateBusinessDays(recyclerView)
                val menuSuccess = updateMenuItems()

                if (businessDaySuccess && menuSuccess) {
                    uploadRestaurantImages(restaurantId)
                    showToast("식당 정보가 성공적으로 저장되었습니다!")
                } else {
                    showToast("일부 항목 저장 실패. 다시 시도하세요.")
                }

            } catch (e: Exception) {
                Log.e("SAVE_ERROR", "저장 중 오류 발생", e)
                showToast("저장 중 오류 발생: ${e.localizedMessage}")
            }
        }

    }

    /**
     * 영업일을 업데이트합니다.
     */
    private suspend fun updateBusinessDays(parentRecyclerView: RecyclerView): Boolean {
        return try {
            val businessDayList = BusinessDayList(
                restaurantId = restaurantId,
                businessDayInfoList = businessDayAdapter.getUpdatedBusinessDays(parentRecyclerView)
            )
            val accessToken = tokenManager.getAccessToken() ?: ""
            val response = RetrofitInstance.restaurantApi.editBusinessDay("Bearer $accessToken",businessDayList)
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
    /**
     * 메뉴를 업데이트합니다.
     */
    private suspend fun updateMenuItems(): Boolean {
        return try {
            val menuItems = menuAdapter.getUpdatedMenuItems()
            var allSuccess = true
            val accessToken = tokenManager.getAccessToken() ?: ""

            for (menu in menuItems) {
                val response = if (menu.menuId == -1) {
                    val imagePart = menu.imageUrl?.let { prepareFilePart("image", Uri.parse(it)) }
                    RetrofitInstance.restaurantApi.uploadMenu(
                        "Bearer $accessToken",restaurantId, menu.name, menu.price, menu.description ?: "", imagePart
                    )
                } else {
                    val request = MenuRequest(
                        id = menu.menuId,
                        restaurantId = restaurantId,
                        name = menu.name,
                        price = menu.price,
                        description = menu.description ?: "",
                        image = menu.imageUrl
                    )
                    RetrofitInstance.restaurantApi.updateMenu("Bearer $accessToken",request)
                }
                if (!response.isSuccessful) allSuccess = false
            }
            allSuccess
        } catch (e: Exception) {
            false
        }
    }


    /**
     * 식당 데이터를 서버에서 로드하고 UI에 바인딩합니다.
     */
    private fun loadRestaurantData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 액세스 토큰 유효성 확인 및 새로 고침
                val accessToken = tokenManager.ensureValidAccessToken()

                if (accessToken.isNullOrEmpty()) {
                    // 로그인 화면으로 이동
                    Toast.makeText(this@ControlRestaurantDetail, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
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
     * 식당 데이터를 UI 요소에 바인딩합니다.
     * @param restaurant 불러온 식당 정보
     */
    private fun bindRestaurantData(restaurant: RestaurantInfo) {
        findViewById<EditText>(R.id.editRestaurantName).setText(restaurant.name)
        findViewById<EditText>(R.id.editRestaurantDescription).setText(restaurant.description)
        findViewById<EditText>(R.id.editRestaurantAddress).setText(restaurant.address)
        findViewById<EditText>(R.id.editRestaurantPhoneNumber).setText(restaurant.phoneNumber ?: "")
        findViewById<TextView>(R.id.restaurantRating).text = "평점: ${restaurant.rating ?: "정보 없음"}"
        findViewById<TextView>(R.id.restaurantReview).text = "리뷰 수: ${restaurant.reviewCount ?: "0"}"
        findViewById<Spinner>(R.id.editRestaurantCategory).setSelection(getCategoryIndex(restaurant.category))
        // 위치 값 저장
        currentLatitude = restaurant.latitude
        currentLongitude = restaurant.longitude
    }
    /**
     * 카테고리를 가져옵니다.
     */
    private fun getCategoryIndex(category: String?): Int {
        val categories = resources.getStringArray(R.array.restaurant_categories)
        return categories.indexOf(category ?: "기타")
    }

    /**
     * 영업일 데이터를 서버에서 로드하고 RecyclerView에 바인딩합니다.
     */
    private fun loadBusinessDays() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 액세스 토큰 유효성 확인 및 새로 고침
                val accessToken = tokenManager.ensureValidAccessToken()

                if (accessToken.isNullOrEmpty()) {
                    // 로그인 화면으로 이동
                    Toast.makeText(this@ControlRestaurantDetail, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
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
     * 메뉴 항목 데이터를 서버에서 로드하고 RecyclerView에 바인딩합니다.
     */
    private fun loadMenuItems() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 액세스 토큰 유효성 확인 및 새로 고침
                val accessToken = tokenManager.ensureValidAccessToken()

                if (accessToken.isNullOrEmpty()) {
                    // 로그인 화면으로 이동
                    Toast.makeText(this@ControlRestaurantDetail, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
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
     * 식당 이미지를 업로드합니다.
     */
    private fun uploadRestaurantImages(restaurantId: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val imageParts = selectedImageUris.mapIndexed { index, uri ->
                    prepareFilePart("images[$index]", uri)
                }
                // 액세스 토큰 유효성 확인 및 새로 고침
                val accessToken = tokenManager.ensureValidAccessToken()

                if (accessToken.isNullOrEmpty()) {
                    // 로그인 화면으로 이동
                    Toast.makeText(this@ControlRestaurantDetail, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val response = RetrofitInstance.restaurantApi.uploadRestaurantImages("Bearer $accessToken",restaurantId, imageParts)
                if (response.isSuccessful) {
                    showToast("이미지 업로드 성공!")
                } else {
                    showToast("이미지 업로드 실패: ${response.code()}")
                }
            } catch (e: Exception) {
                showToast("이미지 업로드 중 오류 발생: ${e.message}")
            }
        }
    }

    /**
     * 식당 이미지를 서버에서 로드하고 UI에 표시합니다.
     */
    private fun loadRestaurantImages() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 액세스 토큰 유효성 확인 및 새로 고침
                val accessToken = tokenManager.ensureValidAccessToken()

                if (accessToken.isNullOrEmpty()) {
                    // 로그인 화면으로 이동
                    Toast.makeText(this@ControlRestaurantDetail, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                val response = RetrofitInstance.restaurantApi.getRestaurantImages(restaurantId)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val images = response.body()?.imageList?.map { it.imageUrl }
                        if (!images.isNullOrEmpty()) {
                            // 첫 번째 이미지를 restaurantImage에 로드
                            Glide.with(this@ControlRestaurantDetail)
                                .load(images.first())
                                .placeholder(R.drawable.placeholder_image) // 로딩 중 이미지
                                .error(R.drawable.ic_uos) // 오류 시 기본 이미지
                                .into(findViewById(R.id.restaurantImage))
                        } else {
                            showToast("이미지가 없습니다.")
                        }
                    } else {
                        showToast("이미지 로딩 실패: ${response.errorBody()?.string()}")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    showToast("이미지 로딩 중 오류 발생: ${e.message}")
                }
            }
        }
    }

    /**
     * 사진 선택기 열기
     */
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    /**
     * 선택된 이미지를 처리하는 메서드
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            val imageUri: Uri? = data.data
            if (imageUri != null) {
                selectedImageUris.add(imageUri)
                addImageToLayout(imageUri)
            }
        }
    }

    /**
     * 이미지 추가 레이아웃에 이미지 표시합니다.
     */
    private fun addImageToLayout(imageUri: Uri?) {
        val imageView = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(200, 200).apply {
                setMargins(8, 8, 8, 8)
            }
            setImageURI(imageUri)
        }
        photoAttachmentLayout.addView(imageView)
        Toast.makeText(this, "이미지가 추가되었습니다.", Toast.LENGTH_SHORT).show()
    }

    /**
     * 이미지 파일을 준비합니다.
     */
    private fun prepareFilePart(partName: String, fileUri: Uri): MultipartBody.Part {
        val fileDescriptor = contentResolver.openFileDescriptor(fileUri, "r") ?: return MultipartBody.Part.createFormData(partName, "")
        val inputStream = contentResolver.openInputStream(fileUri)
        val file = File(cacheDir, contentResolver.getFileName(fileUri))

        file.outputStream().use { output ->
            inputStream?.copyTo(output)
        }

        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(partName, file.name, requestFile)
    }

    /**
     * 파일 이름 가져옵니다.
     */
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



    /**
     * 토스트 메시지를 UI에 표시합니다.
     * @param message 표시할 메시지
     */
    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }


}
