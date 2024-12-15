package com.example.uosense

import TokenManager
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.uosense.AppUtils.showToast
import com.example.uosense.adapters.RestaurantListAdapter
import com.example.uosense.adapters.RestaurantViewType
import com.example.uosense.databinding.ActivityControlRestaurantListBinding
import com.example.uosense.databinding.ActivityRestaurantListBinding
import com.example.uosense.models.RestaurantListResponse
import com.example.uosense.network.RetrofitInstance
import com.example.uosense.network.RetrofitInstance.restaurantApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
/**
 * **ControlRestaurantListActivity**
 *
 * 식당 관리 기능을 제공하는 액티비티로,
 * 관리자 권한으로 식당 목록을 관리할 수 있습니다.
 */
class ControlRestaurantListActivity : AppCompatActivity() {

    private lateinit var originalRestaurantList: MutableList<RestaurantListResponse>
    private lateinit var restaurantList: MutableList<RestaurantListResponse>
    private lateinit var adapter: RestaurantListAdapter
    private lateinit var binding: ActivityControlRestaurantListBinding
    private var selectedFilter = "DEFAULT"
    //검색을 위해서 새로운 변수
    private var selectedDoorType: String? = null

    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tokenManager = TokenManager(this)
        // 바인딩 초기화
        binding = ActivityControlRestaurantListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        originalRestaurantList = intent.getParcelableArrayListExtra("restaurantList") ?: mutableListOf()
        restaurantList = ArrayList(originalRestaurantList)

        setupRecyclerView()

        setupSearch()

        setupFilterButtons()

        setupClickListeners()

        setupSortButton()

        checkIfListIsEmpty()

        customizeSearchView()

        binding.ivMap.setOnClickListener {
            val intent = Intent(this, ControlMainActivity::class.java)
            startActivity(intent)
        }
    }
    /**
     * RecyclerView 초기 설정
     */
    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        // 어댑터 초기화 시 CONTROL_VIEW 모드 설정
        adapter = RestaurantListAdapter(
            restaurantList,
            onItemClick = { navigateToDetailActivity(it) },
            onDeleteClick = { restaurant -> confirmDeleteRestaurant(restaurant) },
            viewType = RestaurantViewType.CONTROL_VIEW // CONTROL_VIEW 사용
        )
        binding.recyclerView.adapter = adapter
    }


    /**
     * 식당 삭제 확인 대화 상자 표시
     * @param restaurant 삭제할 식당 객체
     */
    private fun confirmDeleteRestaurant(restaurant: RestaurantListResponse) {
        AlertDialog.Builder(this)
            .setTitle("삭제 확인")
            .setMessage("정말로 ${restaurant.name} 식당을 정말로 식당 삭제? (주의)")
            .setPositiveButton("삭제") { _, _ -> deleteRestaurant(restaurant.id) }
            .setNegativeButton("취소", null)
            .show()
    }

    /**
     * 식당 삭제 API 호출
     * @param restaurantId 삭제할 식당 ID
     */
    private fun deleteRestaurant(restaurantId: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            val accessToken = tokenManager.ensureValidAccessToken()
            if (accessToken.isNullOrEmpty()) {
                showToast(this@ControlRestaurantListActivity, "로그인이 필요합니다.")
                return@launch
            }

            try {
                val response = RetrofitInstance.restaurantApi.deleteRestaurant("Bearer $accessToken", restaurantId)
                if (response.isSuccessful) {
                    restaurantList.removeAll { it.id == restaurantId }
                    adapter.updateList(restaurantList)
                    showToast(this@ControlRestaurantListActivity, "삭제되었습니다.")
                } else {
                    showToast(this@ControlRestaurantListActivity, "삭제 실패.")
                }
            } catch (e: Exception) {
                showToast(this@ControlRestaurantListActivity, "삭제 중 오류 발생.")
            }
        }
    }

    /**
     * 식당 목록이 비어 있는지 확인
     * 비어 있으면 "결과 없음" 텍스트 표시
     */
    private fun checkIfListIsEmpty() {
        if (restaurantList.isEmpty()) {
            binding.recyclerView.visibility = RecyclerView.GONE
            binding.tvNoResults.visibility = TextView.VISIBLE
        } else {
            binding.recyclerView.visibility = RecyclerView.VISIBLE
            binding.tvNoResults.visibility = TextView.GONE
        }
    }
    /**
     * 필터 버튼 클릭 리스너 등록
     */
    private fun setupFilterButtons() {
        binding.apply {
            doorTypeButton1.setOnClickListener { filterRestaurantsLocally("정문") }
            doorTypeButton2.setOnClickListener { filterRestaurantsLocally("쪽문") }
            doorTypeButton3.setOnClickListener { filterRestaurantsLocally("후문") }
            doorTypeButton4.setOnClickListener { filterRestaurantsLocally("남문")}
        }
    }


    /**
     * DoorType에 따른 식당 목록 필터링
     * @param doorType 선택된 문 유형
     */
    private fun filterRestaurantsLocally(doorType: String) {
        selectedDoorType = doorType

        // DoorType에 해당하는 필터된 리스트 생성
        val filteredList = originalRestaurantList.filter { it.doorType == doorType }

        if (filteredList.isEmpty()) {
            showToast(this, "선택된 필터에 해당하는 식당이 없습니다.")
        }

        // 어댑터에 필터된 결과 업데이트
        restaurantList.clear()
        restaurantList.addAll(filteredList)
        adapter.updateList(filteredList)  // 필터 결과 전달

        checkIfListIsEmpty()  // 결과가 없으면 "결과 없음" 표시
    }


    /**
    * 정렬 버튼 클릭 리스너 등록
    */
    private fun setupSortButton() {
        binding.btnFilter.setOnClickListener {
            showSortOptions()
        }
    }
    /**
     * 정렬 기준 선택 대화 상자 표시
     */
    private fun showSortOptions() {
        val sortOptions = arrayOf("리뷰 많은 순", "즐겨찾기 많은 순", "평점 순", "가격 낮은 순", "거리 가까운 순")
        val apiValues = arrayOf("REVIEW", "BOOKMARK", "RATING", "PRICE", "DISTANCE")

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("정렬 기준 선택")
            .setItems(sortOptions) { _, which ->
                selectedFilter = apiValues[which]
                binding.tvSelectedSort.text = sortOptions[which]
                fetchSortedRestaurants()
            }
            .setNegativeButton("취소", null)
            .show()
    }
    /**
     * 정렬된 식당 목록을 서버에서 불러옴
     */
    private fun fetchSortedRestaurants() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.restaurantApi.sortRestaurants("", selectedFilter)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                        adapter.updateList(response.body()!!)
                    } else {
                        adapter.updateList(emptyList())
                    }
                    checkIfListIsEmpty()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ControlRestaurantListActivity, "정렬 중 오류 발생", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    /**
     * 식당 세부 정보 화면으로 이동
     * @param restaurant 선택된 식당 객체
     */
    private fun navigateToDetailActivity(restaurant: RestaurantListResponse) {
        val intent = Intent(this, ControlRestaurantDetail::class.java).apply {
            putExtra("restaurantId", restaurant.id)
        }
        startActivity(intent)
    }
    /**
     * 검색어를 입력받아 식당 목록을 필터링합니다.
     */
    private fun setupSearch() {
        binding.svSearch.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                val trimmedQuery = query?.trim()
                if (!trimmedQuery.isNullOrBlank()) {
                    searchRestaurants(trimmedQuery)
                } else {
                    showToast(this@ControlRestaurantListActivity, "검색어를 입력해주세요.")
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean = false
        })
    }
    /**
     * doorType을 API에서 사용하는 값으로 매핑하는 함수
     */
    private fun mapDoorTypeForApi(doorType: String): String {
        return when (doorType) {
            "정문" -> "FRONT"
            "남문" -> "SOUTH"
            "쪽문" -> "SIDE"
            "후문" -> "BACK"
            else -> "NULL"  // 기본값 처리
        }
    }
    /**
     * API를 통해 검색어와 선택된 문 유형(DoorType)에 따라 식당 목록을 검색합니다.
     * @param keyword 검색할 키워드
     */
    private fun searchRestaurants(keyword: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiDoorType = if (!selectedDoorType.isNullOrEmpty()) {
                    mapDoorTypeForApi(selectedDoorType!!)
                }else {
                    null
                }
                val response = restaurantApi.searchRestaurants(
                    keyword = keyword,
                    doorType = apiDoorType
                )
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null && response.body()!!.isNotEmpty()) {
                        if (selectedDoorType != null) {
                            navigateToSelectedDoorList(response.body()!!, mapDoorTypeForApi(selectedDoorType!!))
                        } else {
                            navigateToRestaurantList(response.body()!!)
                        }
                    } else {
                        showToast(this@ControlRestaurantListActivity, "검색 결과가 없습니다.")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {

                }
            }
        }
    }
    /**
     * 선택된 필터에 따른 식당 리스트 화면으로 이동
     * @param restaurantList 검색된 식당 리스트
     * @param doorType 선택된 필터
     */
    private fun navigateToSelectedDoorList(
        restaurantList: List<RestaurantListResponse>,
        doorType: String
    ) {
        val intent = Intent(this, SelectedDoorActivity::class.java).apply {
            putParcelableArrayListExtra("restaurantList", ArrayList(restaurantList))
            putExtra("doorType", doorType)
        }
        startActivity(intent)
    }
    /**
     * 식당 리스트 화면으로 이동
     * @param restaurantList 검색된 식당 리스트
     */
    private fun navigateToRestaurantList(restaurantList: List<RestaurantListResponse>) {
        val intent = Intent(this, ControlRestaurantListActivity::class.java).apply {
            putParcelableArrayListExtra("restaurantList", ArrayList(restaurantList))
            putExtra("defaultDoorType", "FRONT") // 정문 기본 선택
        }
        startActivity(intent)
    }



    /**
     * 검색창 글씨체는 따로 함수정의해서 색상 변경합니다.
     */
    private fun customizeSearchView() {
        try {
            // SearchView의 AutoCompleteTextView 가져오기
            val searchAutoComplete = binding.svSearch.javaClass
                .getDeclaredField("mSearchSrcTextView")
                .apply { isAccessible = true }
                .get(binding.svSearch) as? android.widget.AutoCompleteTextView

            searchAutoComplete?.apply {
                setHintTextColor(resources.getColor(R.color.black, null))  // 힌트 텍스트 색상 설정
                setTextColor(resources.getColor(R.color.black, null))     // 입력 텍스트 색상 설정
                textSize = 16f                                            // 텍스트 크기 설정
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 검색창 초기화 및 검색 기능 설정
     */
    private fun setupClickListeners() {
        binding.ivSearch.setOnClickListener {
            val query = binding.svSearch.query.toString().trim()
            if (query.isNotBlank()) {
                searchRestaurants(query)
            } else {
                Toast.makeText(this, "검색어를 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
