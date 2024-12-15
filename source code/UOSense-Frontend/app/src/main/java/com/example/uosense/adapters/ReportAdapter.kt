package com.example.uosense.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.uosense.R
import com.example.uosense.models.ReportResponse
import com.example.uosense.network.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReportAdapter(
    private var reports: MutableList<ReportResponse>
) : RecyclerView.Adapter<ReportAdapter.ReportViewHolder>() {

    inner class ReportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val reportId: TextView = itemView.findViewById(R.id.reportId)
        val reviewId: TextView = itemView.findViewById(R.id.reviewId)
        val userId: TextView = itemView.findViewById(R.id.userId)
        val reportDetail: TextView = itemView.findViewById(R.id.reportDetail)
        val createdAt: TextView = itemView.findViewById(R.id.createdAt)
        val deleteReportBtn: Button = itemView.findViewById(R.id.deleteReportBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_report, parent, false)
        return ReportViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val report = reports[position]

        holder.reportId.text = "신고 ID: ${report.reportId}"
        holder.reviewId.text = "리뷰 ID: ${report.reviewId}"
        holder.userId.text = "사용자 ID: ${report.userId}"
        // 날짜 포맷 변환
        holder.createdAt.text = "신고 일시: ${formatDate(report.createdAt)}"
        holder.reportDetail.text = "신고 사유: ${report.detail}"

        // 삭제 버튼 클릭 리스너
        holder.deleteReportBtn.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val response = RetrofitInstance.restaurantApi.deleteReview(report.reviewId)

                    if (response.isSuccessful) {
                        Toast.makeText(
                            holder.itemView.context,
                            "리뷰가 삭제되었습니다.",
                            Toast.LENGTH_SHORT
                        ).show()

                        // 삭제된 항목 제거 및 UI 업데이트
                        reports.removeAt(position)
                        notifyItemRemoved(position)
                    } else {
                        Toast.makeText(
                            holder.itemView.context,
                            "삭제 실패: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        holder.itemView.context,
                        "오류 발생: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    // 날짜 포맷팅 함수 추가
    private fun formatDate(dateTime: List<Int>): String {
        return if (dateTime.size >= 6) {
            "%04d-%02d-%02d %02d:%02d".format(
                dateTime[0], dateTime[1], dateTime[2], dateTime[3], dateTime[4]
            )
        } else {
            "날짜 정보 없음"
        }
    }

    override fun getItemCount(): Int = reports.size
}
