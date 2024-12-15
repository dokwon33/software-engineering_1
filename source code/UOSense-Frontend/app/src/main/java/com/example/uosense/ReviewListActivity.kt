package com.example.uosense

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.uosense.adapters.ReviewAdapter
import com.example.uosense.models.ReviewItem
import com.example.uosense.network.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReviewListActivity : AppCompatActivity() {

    /** UI 요소 및 어댑터 선언 */
    private lateinit var reviewRecyclerView: RecyclerView
    private lateinit var reviewAdapter: ReviewAdapter
    private val reviews = mutableListOf<ReviewItem>()
    private var restaurantId: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review_list)

        /** 식당 ID 수신 */
        restaurantId = intent.getIntExtra("restaurantId", 1)

        /** RecyclerView 설정 */
        reviewRecyclerView = findViewById(R.id.reviewRecyclerView)
        reviewRecyclerView.layoutManager = LinearLayoutManager(this)

        /** 어댑터 초기화 */
        reviewAdapter = ReviewAdapter(reviews) { holder ->
            holder.reportBtn.visibility = View.VISIBLE /** 신고 버튼 표시 */
            holder.deleteBtn.visibility = View.GONE   /** 삭제 버튼 숨기기 */
        }
        reviewRecyclerView.adapter = reviewAdapter

        /** 리뷰 데이터 가져오기 */
        fetchReviews()
    }

    /** 리뷰 목록 가져오는 메서드 */
    private fun fetchReviews() {
        val restaurantId = restaurantId

        CoroutineScope(Dispatchers.Main).launch {
            try {
                /** 로딩 UI 표시 */
                showLoading()

                /** API 호출하여 리뷰 데이터 가져오기 */
                val response = RetrofitInstance.restaurantApi.getRestaurantReviews(restaurantId)

                Log.d("ReviewAdapter", "리뷰 목록 크기: ${reviews.size}")

                if (response.isSuccessful) {
                    val reviewList = response.body() ?: emptyList()

                    /** 데이터 확인 로그 */
                    reviewList.forEach { Log.d("ReviewItem", it.toString()) }

                    if (reviewList.isEmpty()) {
                        Toast.makeText(
                            this@ReviewListActivity,
                            "리뷰가 없습니다.",
                            Toast.LENGTH_SHORT
                        ).show() /** 리뷰가 없으면 메시지 표시 */
                    } else {
                        /** 기존 데이터 지우고 새 데이터 추가 */
                        reviews.clear()
                        reviews.addAll(reviewList)
                        reviewAdapter.notifyDataSetChanged() /** 어댑터에 데이터 업데이트 알림 */
                        Log.d("ReviewList", "Fetched ${reviews.size} reviews successfully")
                    }
                } else {
                    /** API 응답 오류 시 메시지 출력 */
                    Toast.makeText(
                        this@ReviewListActivity,
                        "리뷰를 가져오는 데 실패했습니다: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                /** 예외 처리 시 메시지 출력 */
                Toast.makeText(
                    this@ReviewListActivity,
                    "오류 발생: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                /** 로딩 UI 숨기기 */
                hideLoading()
            }
        }
    }

    /** 로딩 UI 표시 메서드 */
    private fun showLoading() {
        /** 로딩 ProgressBar 또는 로딩 상태 UI 표시 (추후 구현 필요) */
    }

    /** 로딩 UI 숨기기 메서드 */
    private fun hideLoading() {
        /** 로딩 ProgressBar 숨기기 (추후 구현 필요) */
    }
}
