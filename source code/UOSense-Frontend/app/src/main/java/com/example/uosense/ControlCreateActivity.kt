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
import com.example.uosense.databinding.ActivityControlMainBinding
import com.example.uosense.databinding.ActivityControlRestaurantDetailBinding
import com.example.uosense.databinding.ActivityRestaurantDetailBinding
import com.example.uosense.databinding.ActivitySelectedDoorBinding
import com.example.uosense.models.BusinessDayInfo
import com.example.uosense.models.BusinessDayList
import com.example.uosense.models.MenuRequest
import com.example.uosense.models.MenuResponse
import com.example.uosense.models.RestaurantInfo
import com.example.uosense.models.RestaurantListResponse
import com.example.uosense.models.RestaurantRequest
import com.example.uosense.network.RetrofitInstance
import com.example.uosense.network.RetrofitInstance.restaurantApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Address
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File

class ControlCreateActivity : AppCompatActivity(), MenuImagePicker {
    /**
     * **openImagePicker**
     * 이미지 선택기를 엽니다.
     */
    override fun openImagePicker(position: Int) {
        currentMenuPosition = position
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }
    private var initialLatitude: Double = 0.0
    private var initialLongitude: Double = 0.0
    // 버튼 및 리사이클러
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

        tokenManager = TokenManager(this)
        setContentView(R.layout.activity_control_create)

        val doorTypeSpinner = findViewById<Spinner>(R.id.editDoorType)
        ArrayAdapter.createFromResource(
            this,
            R.array.restaurant_door_types,
            R.layout.spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
            doorTypeSpinner.adapter = adapter
        }

        val categorySpinner = findViewById<Spinner>(R.id.editRestaurantCategory)
        ArrayAdapter.createFromResource(
            this,
            R.array.restaurant_categories,
            R.layout.spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
            categorySpinner.adapter = adapter
        }
        val subDescriptionSpinner = findViewById<Spinner>(R.id.editSubDescription)
        ArrayAdapter.createFromResource(
            this,
            R.array.restaurant_sub_descriptions,
            R.layout.spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
            subDescriptionSpinner.adapter = adapter
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
                    dayOfWeek = "",
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


    }


    /**
     * **saveRestaurantData**
     * 사용자가 입력한 식당 데이터를 저장합니다.
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


        // 도로명 주소를 통해 위도/경도 가져오기
        getLatLngFromAddress(address) { latitude, longitude ->
            if (latitude == null || longitude == null) {
                showToast("위치 검색 실패: 주소를 확인하세요.")
                return@getLatLngFromAddress
            }

            CoroutineScope(Dispatchers.Main).launch {
                try {
                    // 액세스 토큰 유효성 확인 및 새로 고침
                    val accessToken = tokenManager.ensureValidAccessToken()

                    if (accessToken.isNullOrEmpty()) {
                        // 로그인 화면으로 이동
                        Toast.makeText(this@ControlCreateActivity, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                    val restaurantResponse = RetrofitInstance.restaurantApi.createRestaurant(
                        "Bearer $accessToken",
                        RestaurantRequest(
                            id = -1,
                            name = name,
                            doorType = AppUtils.mapDoorTypeForApi(doorType),
                            latitude = latitude,
                            longitude = longitude,
                            address = address,
                            phoneNumber = phoneNumber,
                            category = AppUtils.mapCategoryForApi(category),
                            subDescription = AppUtils.mapSubDescription(subDescription),
                            description = description
                        )
                    )

                    if (restaurantResponse.isSuccessful) {
                        val createdRestaurantId = restaurantResponse.body()?.id ?: -1
                        if (createdRestaurantId != -1) {
                            val businessDayResponse = updateBusinessDays(recyclerView)
                            val menuResponse = updateMenuItems()
                            val imageResponse = uploadRestaurantImages(createdRestaurantId)

                            if (businessDayResponse && menuResponse && imageResponse) {
                                showToast("식당이 성공적으로 등록되었습니다!")
                                finish()
                            } else {
                                showToast("일부 항목 저장에 실패했습니다.")
                            }
                        } else {
                            showToast("식당 생성 오류가 발생했습니다.")
                        }
                    } else {
                        showToast("등록 실패: ${restaurantResponse.code()}")
                    }
                } catch (e: Exception) {
                    finish()
                }
            }
        }
    }



    // 영업일
    private suspend fun updateBusinessDays(parentRecyclerView: RecyclerView): Boolean {
        return try {
            val businessDayList = BusinessDayList(
                restaurantId = restaurantId,
                businessDayInfoList = businessDayAdapter.getUpdatedBusinessDays(parentRecyclerView)
            )
            val accessToken = tokenManager.getAccessToken() ?: ""
            val response = RetrofitInstance.restaurantApi.createBusinessDay("Bearer $accessToken",businessDayList)
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun updateMenuItems(): Boolean {
        return try {
            val menuItems = menuAdapter.getUpdatedMenuItems()
            var allSuccess = true
            val accessToken = tokenManager.getAccessToken() ?: ""

            for (menu in menuItems) {
                val response = if (menu.menuId == -1) {
                    val imagePart = menu.imageUrl?.let { prepareFilePart("image", Uri.parse(it)) }
                    RetrofitInstance.restaurantApi.uploadMenu(
                        "Bearer $accessToken",
                        restaurantId, menu.name, menu.price, menu.description ?: "", imagePart
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



    private suspend fun uploadRestaurantImages(restaurantId: Int): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val imageParts = selectedImageUris.mapIndexed { index, uri ->
                    prepareFilePart("images[$index]", uri)
                }
                val accessToken = tokenManager.getAccessToken() ?: ""
                val response = RetrofitInstance.restaurantApi.uploadRestaurantImages(
                    "Bearer $accessToken", restaurantId, imageParts
                )
                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) { showToast("이미지 업로드 성공!") }
                    true
                } else {
                    withContext(Dispatchers.Main) {
                        showToast("이미지 업로드 실패: ${response.code()}")
                    }
                    false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("이미지 업로드 중 오류 발생: ${e.message}")
                }
                false
            }
        }
    }



    /**
     * **openImagePicker**
     * 이미지 선택기를 엽니다.
     */
    private fun openImagePicker() {
        currentMenuPosition = -1  // 식당 이미지 선택을 의미
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }
    /**
     * **onActivityResult**
     * 이미지 선택 결과를 처리합니다.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            val imageUri: Uri = data.data!!

            if (currentMenuPosition != -1) {
                // 메뉴 이미지 업데이트
                menuAdapter.updateMenuImage(currentMenuPosition, imageUri)
                currentMenuPosition = -1
            } else {
                // 식당 이미지 추가
                selectedImageUris.add(imageUri)
                addImageToLayout(imageUri)
            }
        }
    }

    /**
     * **addImageToLayout**
     * 선택한 이미지를 레이아웃에 추가합니다.
     * @param imageUri 이미지 URI
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
     * 이미지 파일을 가져옵니다.
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
     * 파일 이름을 가져옵니다.
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



    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }





    /**
     * 도로명 주소를 위도와 경도로 변환합니다.
     * @param restaurant 변환할 식당 정보 객체
     * @param callback 변환된 위도 및 경도를 반환하는 콜백 함수
     */
    private fun getLatLngFromAddress(address: String, callback: (Double?, Double?) -> Unit) {
        val client = OkHttpClient()
        val url = "https://naveropenapi.apigw.ntruss.com/map-geocode/v2/geocode?query=${address}"

        // ID, KEY 절대 수정 X , SECRET 사용 가능
        val request = Request.Builder()
            .url(url)
            .addHeader("X-NCP-APIGW-API-KEY-ID", "s78aa7asq0")
            .addHeader("X-NCP-APIGW-API-KEY", "WGmu5zQXqTGWOy7Bj9PWwrD8HeQezlBvZ675Q24K")
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()
                val json = JSONObject(responseBody ?: "")
                val addresses = json.getJSONArray("addresses")

                if (addresses.length() > 0) {
                    val location = addresses.getJSONObject(0)
                    val latitude = location.getDouble("y")
                    val longitude = location.getDouble("x")

                    withContext(Dispatchers.Main) {
                        callback(latitude, longitude)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        callback(null, null)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    callback(null, null)
                }
            }
        }
    }
}
