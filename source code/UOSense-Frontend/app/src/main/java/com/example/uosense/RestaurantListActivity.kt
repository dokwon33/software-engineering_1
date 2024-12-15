package com.example.uosense

import TokenManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.uosense.AppUtils.showToast
import com.example.uosense.adapters.RestaurantListAdapter
import com.example.uosense.adapters.RestaurantViewType
import com.example.uosense.databinding.ActivityRestaurantListBinding
import com.example.uosense.models.RestaurantListResponse
import com.example.uosense.network.RetrofitInstance
import com.example.uosense.network.RetrofitInstance.restaurantApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
/**
 * **RestaurantListActivity**
 *
 * 사용자가 특정 필터 또는 검색어로 식당 목록을 조회하는 액티비티입니다.
 */
class RestaurantListActivity : AppCompatActivity() {

    private lateinit var originalRestaurantList: MutableList<RestaurantListResponse>
    private lateinit var restaurantList: MutableList<RestaurantListResponse>
    private lateinit var adapter: RestaurantListAdapter
    private lateinit var binding: ActivityRestaurantListBinding
    private var selectedFilter = "DEFAULT"
    private lateinit var tokenManager: TokenManager
    //검색을 위해서 새로운 변수
    private var selectedDoorType: String? = null
    private var keyword : String? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tokenManager = TokenManager(this)

        // 바인딩 초기화
        binding = ActivityRestaurantListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 의도(Intent)에서 값 가져오기
        val isSearchResult = intent.getBooleanExtra("isSearchResult", false)

        val skeyword = intent.getStringExtra("keyword")

        keyword = skeyword
        // sortBox 가시성 설정
        binding.sortBox.visibility = if (isSearchResult) View.VISIBLE else View.GONE

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
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
    /**
     * RecyclerView를 초기화하고 어댑터를 설정합니다.
     */
    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        // 어댑터 초기화 시 CONTROL_VIEW 모드 설정
        adapter = RestaurantListAdapter(
            restaurantList,
            onItemClick = { navigateToDetailActivity(it) },
            onDeleteClick = { },
            viewType = RestaurantViewType.USER_VIEW // CONTROL_VIEW 사용
        )
        binding.recyclerView.adapter = adapter
    }
    /**
     * 식당 목록이 비어 있는지 확인하고 결과가 없을 경우 메시지를 표시합니다.
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
     * DoorType 필터 버튼의 클릭 리스너를 설정합니다.
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
     * 특정 문 유형(DoorType)에 따라 식당 목록을 필터링합니다.
     * @param doorType 필터링할 문 유형
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
     * 정렬 버튼 클릭 리스너를 설정합니다.
     */
    private fun setupSortButton() {
        binding.btnFilter.setOnClickListener {
            showSortOptions()
        }
    }
    /**
     * 정렬 기준을 선택하는 다이얼로그를 표시합니다.
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
     * 선택된 기준에 따라 정렬된 식당 목록을 API에서 불러옵니다.
     */
    private fun fetchSortedRestaurants() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val accessToken = tokenManager.ensureValidAccessToken()
                if (accessToken.isNullOrEmpty()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@RestaurantListActivity, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                val response = RetrofitInstance.restaurantApi.sortRestaurants(
                    keyword.orEmpty(),
                    selectedFilter
                )

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                        adapter.updateList(response.body()!!)
                    } else {
                        adapter.updateList(emptyList())
                        showToast(this@RestaurantListActivity, "결과가 없습니다.")
                    }
                    checkIfListIsEmpty()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@RestaurantListActivity, "정렬 중 오류 발생", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    /**
     * 선택된 식당의 상세 정보를 보여주는 액티비티로 이동합니다.
     * @param restaurant 식당 데이터
     */
    private fun navigateToDetailActivity(restaurant: RestaurantListResponse) {
        val intent = Intent(this, RestaurantDetailActivity::class.java).apply {
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
                    showToast(this@RestaurantListActivity, "검색어를 입력해주세요.")
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
                // 액세스 토큰 유효성 확인 및 새로 고침
                val accessToken = tokenManager.ensureValidAccessToken()

                if (accessToken.isNullOrEmpty()) {
                    // 로그인 화면으로 이동
                    Toast.makeText(this@RestaurantListActivity, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                    return@launch
                }
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
                        showToast(this@RestaurantListActivity, "검색 결과가 없습니다.")
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
     * 선택된 필터(doortype)의 상세 정보를 보여주는 액티비티로 이동합니다.
     * @param restaurantList 식당 리스트
     * @param doorType 식당 필터(doorType : 정문, 후문, 쪽문, 남문)
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
     * 검색된 식당 리스트를 보여주는 액티비티로 이동합니다.
     * @param restaurantList 식당 리스트
     */
    private fun navigateToRestaurantList(restaurantList: List<RestaurantListResponse>,isSearchResult: Boolean = false) {
        val intent = Intent(this, RestaurantListActivity::class.java).apply {
            putParcelableArrayListExtra("restaurantList", ArrayList(restaurantList))
            putExtra("defaultDoorType", "FRONT") // 정문 기본 선택
            putExtra("isSearchResult", isSearchResult)
        }
        startActivity(intent)
    }



    /**
     * 검색창 글씨체는 따로 함수정의해서 색상 변경
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
     * 검색창 사용자 정의
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
