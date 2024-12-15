package com.example.uosense

import TokenManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.uosense.adapters.FavoriteAdapter
import com.example.uosense.network.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * **FavoriteListActivity**
 *
 * 내 즐겨찾기 목록을 표시하는 액티비티입니다.
 * 내가 즐겨찾기한 식당 조회 기능을 제공합니다.
 */


class FavoriteListActivity : AppCompatActivity() {

    /** RecyclerView 및 어댑터 선언 */
    private lateinit var recyclerView: RecyclerView
    private lateinit var favoriteAdapter: FavoriteAdapter

    /** 토큰 관리 및 버튼 선언 */
    private lateinit var tokenManager: TokenManager
    private lateinit var backBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite_list)

        /** 토큰 관리 객체 초기화 */
        tokenManager = TokenManager(this)

        /** 뒤로 가기 버튼 설정 */
        backBtn = findViewById(R.id.backBtn)
        backBtn.setOnClickListener {
            navigateToMyPage() /** 뒤로 가기 버튼 클릭 시 마이페이지로 이동 */
        }

        /** RecyclerView 설정 및 즐겨찾기 목록 가져오기 */
        setupRecyclerView()
        fetchFavorites()
    }

    /** RecyclerView 초기 설정 */
    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.favoriteRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this) /** 세로 방향으로 리스트 표시 */
        favoriteAdapter = FavoriteAdapter(emptyList()) /** 초기 데이터 비어 있는 어댑터 생성 */
        recyclerView.adapter = favoriteAdapter /** 어댑터 연결 */
    }

    /** 즐겨찾기 목록 서버에서 가져오기 */
    private fun fetchFavorites() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                /** 유효한 액세스 토큰 확인 및 가져오기 */
                val accessToken = tokenManager.ensureValidAccessToken()
                if (accessToken.isNullOrEmpty()) {
                    showToast("로그인이 필요합니다.") /** 토큰이 없으면 메시지 표시 */
                    return@launch
                }

                /** 서버 API 호출하여 즐겨찾기 목록 가져오기 */
                val favorites = RetrofitInstance.restaurantApi.getMyBookmarks("Bearer $accessToken")
                if (favorites.isNotEmpty()) {
                    /** 데이터가 있으면 어댑터에 업데이트 */
                    favoriteAdapter.updateData(favorites)
                    recyclerView.visibility = View.VISIBLE /** RecyclerView 표시 */
                } else {
                    showToast("즐겨찾기 목록이 비어 있습니다.") /** 데이터가 없으면 메시지 표시 */
                }
            } catch (e: Exception) {
                /** 오류 발생 시 메시지 표시 */
                showToast("오류 발생: ${e.message}")
            }
        }
    }

    /** 마이페이지로 이동하는 메서드 */
    private fun navigateToMyPage() {
        val intent = Intent(this, MyPageActivity::class.java)
        startActivity(intent)
        finish() /** 현재 액티비티 종료 */
    }

    /** 토스트 메시지 표시 메서드 */
    private fun showToast(message: String) {
        Toast.makeText(this@FavoriteListActivity, message, Toast.LENGTH_SHORT).show()
    }
}
