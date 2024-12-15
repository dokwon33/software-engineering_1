package com.example.uosense

import TokenManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.uosense.adapters.ReportAdapter
import com.example.uosense.databinding.ActivityReportBinding
import com.example.uosense.models.ReportResponse
import com.example.uosense.network.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * **ReportActivity**
 *
 * 관리자가 신고가 들어온 리뷰를 조회하는 액티비티입니다.
 * 신고된 리뷰 확인 및 리뷰 삭제 기능을 제공합니다.
 */

class ReportActivity : AppCompatActivity() {

    /** 바인딩 객체 및 어댑터 선언 */
    private lateinit var binding: ActivityReportBinding
    private lateinit var reportAdapter: ReportAdapter

    /** 토큰 관리 및 신고 목록 변수 선언 */
    private lateinit var tokenManager: TokenManager
    private val reports = mutableListOf<ReportResponse>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /** 뷰 바인딩 초기화 */
        binding = ActivityReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /** 토큰 관리 객체 초기화 */
        tokenManager = TokenManager(this)

        /** 리사이클러 뷰 설정 및 신고 내역 로드 */
        setupRecyclerView()
        fetchReports()

        /** 뒤로 가기 버튼 클릭 리스너 설정 */
        binding.backBtn.setOnClickListener { finish() }
    }

    /**
     * 리사이클러 뷰 초기 설정 메서드
     */
    private fun setupRecyclerView() {
        reportAdapter = ReportAdapter(reports)
        binding.reportRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ReportActivity) /** 세로형 레이아웃 설정 */
            adapter = reportAdapter /** 어댑터 연결 */
        }
    }

    /**
     * 신고 내역 가져오는 메서드
     */
    private fun fetchReports() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                Log.d("ReportActivity", "Fetching reports...")

                /** 액세스 토큰 확인 및 가져오기 */
                val accessToken = tokenManager.ensureValidAccessToken()
                if (accessToken.isNullOrEmpty()) {
                    showToast("로그인이 필요합니다.") /** 토큰이 없으면 메시지 출력 */
                    Log.e("ReportActivity", "Access token is empty")
                    return@launch
                }

                /** API 요청을 통해 신고 내역 가져오기 */
                val response = RetrofitInstance.restaurantApi.getReports("Bearer $accessToken")

                if (response.isNotEmpty()) {
                    /** 데이터가 있으면 리스트 초기화 후 추가 */
                    reports.clear()
                    reports.addAll(response)
                    reportAdapter.notifyDataSetChanged() /** 어댑터에 데이터 변경 알림 */
                    showToast("리뷰가 신고되었습니다.")
                    Log.d("ReportActivity", "Fetched ${response.size} reports successfully")
                } else {
                    showToast("신고된 리뷰가 없습니다.") /** 데이터가 없으면 메시지 출력 */
                    Log.d("ReportActivity", "No reports found")
                }
            } catch (e: Exception) {
                /** 오류 발생 시 메시지 및 로그 출력 */
                showToast("오류 발생: ${e.message}")
                Log.e("ReportActivity", "Error fetching reports", e)
            }
        }
    }

    /**
     * 메시지 출력 메서드
     */
    private fun showToast(message: String) {
        Toast.makeText(this@ReportActivity, message, Toast.LENGTH_SHORT).show()
    }
}
