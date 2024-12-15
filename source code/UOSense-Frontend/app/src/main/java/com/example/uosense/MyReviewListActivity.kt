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
import com.example.uosense.adapters.ReviewAdapter
import com.example.uosense.network.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * **MyReviewListActivity**
 *
 * 내 리뷰 목록을 표시하는 액티비티입니다.
 * 내 리뷰 목록 조회 기능을 제공합니다.
 */

class MyReviewListActivity : AppCompatActivity() {

    /** RecyclerView 및 어댑터 선언 */
    private lateinit var recyclerView: RecyclerView
    private lateinit var reviewAdapter: ReviewAdapter

    /** UI 요소 및 토큰 관리 객체 선언 */
    private lateinit var backBtn: Button
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_review_list)

        /** 토큰 관리 객체 초기화 */
        tokenManager = TokenManager(this)

        /** UI 초기화 */
        recyclerView = findViewById(R.id.reviewRecyclerView)
        backBtn = findViewById(R.id.backBtn)

        /** 뒤로 가기 버튼 클릭 시 마이페이지로 이동 */
        backBtn.setOnClickListener {
            navigateToMyPage()
        }

        /** RecyclerView 설정 및 데이터 로드 */
        setupRecyclerView()
        fetchMyReviews()
    }

    /**
     * RecyclerView 초기 설정 메서드
     */
    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this) /** 세로형 레이아웃 설정 */
        reviewAdapter = ReviewAdapter(emptyList()) { holder ->
            holder.reportBtn.visibility = View.GONE /** 신고 버튼 숨기기 */
            holder.deleteBtn.visibility = View.VISIBLE /** 삭제 버튼 표시 */
        }
        recyclerView.adapter = reviewAdapter /** 어댑터 연결 */
    }

    /**
     * 사용자 리뷰 목록 가져오기
     */
    private fun fetchMyReviews() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                /** 유효한 액세스 토큰 확인 및 가져오기 */
                val accessToken = tokenManager.ensureValidAccessToken()
                if (accessToken.isNullOrEmpty()) {
                    showToast("로그인이 필요합니다.") /** 토큰이 없으면 메시지 출력 */
                    return@launch
                }

                /** API 호출하여 리뷰 목록 가져오기 */
                val reviews = RetrofitInstance.restaurantApi.getMyReviews("Bearer $accessToken")
                if (reviews.isNotEmpty()) {
                    /** 데이터가 있으면 어댑터 업데이트 */
                    reviewAdapter = ReviewAdapter(reviews) { holder ->
                        holder.reportBtn.visibility = View.GONE /** 신고 버튼 숨기기 */
                        holder.deleteBtn.visibility = View.VISIBLE /** 삭제 버튼 표시 */
                    }
                    recyclerView.adapter = reviewAdapter
                    recyclerView.visibility = View.VISIBLE /** RecyclerView 표시 */
                } else {
                    showToast("작성한 리뷰가 없습니다.") /** 데이터가 없으면 메시지 출력 */
                }
            } catch (e: Exception) {
                showToast("오류 발생: ${e.message}") /** 예외 처리 메시지 출력 */
            }
        }
    }

    /**
     * 메시지 표시 메서드
     */
    private fun showToast(message: String) {
        Toast.makeText(this@MyReviewListActivity, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * 마이페이지로 이동 메서드
     */
    private fun navigateToMyPage() {
        val intent = Intent(this, MyPageActivity::class.java)
        startActivity(intent)
        finish() /** 현재 액티비티 종료 */
    }
}
