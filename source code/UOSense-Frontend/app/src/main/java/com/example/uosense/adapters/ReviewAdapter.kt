package com.example.uosense.adapters

import TokenManager
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.uosense.R
import com.example.uosense.models.ReviewItem
import android.view.ViewGroup
import android.view.LayoutInflater
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.example.uosense.models.ReportRequest
import com.example.uosense.network.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.joda.time.LocalDateTime

class ReviewAdapter(private val reviews: List<ReviewItem>,
                    private val configureButtons: (ReviewViewHolder) -> Unit) :
    RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    inner class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: ImageView = itemView.findViewById(R.id.profileImage)
        val reviewerName: TextView = itemView.findViewById(R.id.reviewerName)
        val reviewRatingBar: RatingBar = itemView.findViewById(R.id.reviewRatingBar)
        val reviewContent: TextView = itemView.findViewById(R.id.reviewContent)
        val eventParticipation: TextView = itemView.findViewById(R.id.eventParticipation)
        val writeDate: TextView = itemView.findViewById(R.id.writeDate)
        val likeCount: TextView = itemView.findViewById(R.id.likeCount)
        val likeCountBtn: Button = itemView.findViewById(R.id.likeCountBtn)
        val reviewImage1: ImageView = itemView.findViewById(R.id.reviewImage1)
        val reviewImage2: ImageView = itemView.findViewById(R.id.reviewImage2)
        val reportBtn: Button = itemView.findViewById(R.id.reportBtn)
        val deleteBtn: Button = itemView.findViewById(R.id.deleteBtn)
        val featureTag1: TextView = itemView.findViewById(R.id.featureTag1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)


    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]

        // 버튼 설정 람다 호출
        configureButtons(holder)

        // 1. 닉네임
        holder.reviewerName.text = review.nickname

        // 2. 별점
        holder.reviewRatingBar.rating = review.rating.toFloat()

        // 3. 리뷰 내용
        holder.reviewContent.text = review.body

        // 4. 이벤트 참여 여부
        holder.eventParticipation.text =
            if (review.reviewEventCheck) "리뷰 이벤트 참여" else "리뷰 이벤트 미참여"

        // 5. 좋아요 수
        holder.likeCount.text = review.likeCount.toString()

        // 6. 작성 날짜
        holder.writeDate.findViewById<TextView>(R.id.writeDate).text = review.getFormattedDate()

        // 7. 프로필 이미지 로드 (Glide 사용)
        Glide.with(holder.itemView.context)
            .load(review.userImage ?: R.drawable.ic_user)
            // 기본 이미지
            .into(holder.profileImage)

        // 8. 리뷰 이미지 로드
        val images = review.imageUrls ?: emptyList()
        holder.reviewImage1.visibility = if (images.isNotEmpty()) View.VISIBLE else View.GONE
        holder.reviewImage2.visibility = if (images.size > 1) View.VISIBLE else View.GONE

        if (images.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(images[0])
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(holder.reviewImage1)
        }
        if (images.size > 1) {
            Glide.with(holder.itemView.context)
                .load(images[1])
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(holder.reviewImage2)
        }

        // 태그 버튼 설정
        if (!review.tag.isNullOrEmpty()) {
            holder.featureTag1.text = review.tag
            holder.featureTag1.visibility = View.VISIBLE

            // 동적 색상 설정
            val (textColorRes, backgroundRes) = when (review.tag) {
                "서비스가 좋아요" -> R.color.green to R.drawable.rounded_border_green
                "데이트 장소 추천" -> R.color.pink to R.drawable.rounded_border_pink
                "혼밥 가능" -> R.color.teal_700 to R.drawable.rounded_border_tealed_700
                "사장님이 친절해요" -> R.color.purple_200 to R.drawable.rounded_border_purple_200
                "인테리어가 멋져요" -> R.color.orange to R.drawable.rounded_border_orange
                else -> R.color.black to R.drawable.rounded_border_black
            }

            // 텍스트 색상 및 백그라운드 적용
            holder.featureTag1.setTextColor(ContextCompat.getColor(holder.itemView.context, textColorRes))
            holder.featureTag1.background = ContextCompat.getDrawable(holder.itemView.context, backgroundRes)

        } else {
            holder.featureTag1.visibility = View.GONE
        }

        // 좋아요 버튼 클릭 이벤트 처리
        holder.likeCountBtn.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    // TokenManager에서 AccessToken 가져오기
                    val accessToken = TokenManager(holder.itemView.context).getAccessToken()
                    if (accessToken.isNullOrEmpty()) {
                        Toast.makeText(
                            holder.itemView.context,
                            "로그인이 필요합니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@launch
                    }

                    // API 요청: 좋아요
                    val response = RetrofitInstance.restaurantApi.likeReview(
                        review.id,
                        "Bearer $accessToken"
                    )

                    when (response.code()) {
                        200 -> {
                            // 좋아요 성공
                            review.likeCount += 1
                            holder.likeCount.text = review.likeCount.toString()
                            Toast.makeText(
                                holder.itemView.context,
                                "리뷰를 추천했습니다!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        403 -> {
                            // 좋아요 제한
                            Toast.makeText(
                                holder.itemView.context,
                                "리뷰 좋아요는 한 번만 가능합니다.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        404 -> {
                            // 리뷰 없음
                            Toast.makeText(
                                holder.itemView.context,
                                "리뷰를 찾을 수 없습니다.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        500 -> {
                            // 서버 오류
                            Toast.makeText(
                                holder.itemView.context,
                                "서버 오류가 발생했습니다.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        else -> {
                            // 기타 예외 처리
                            Toast.makeText(
                                holder.itemView.context,
                                "알 수 없는 오류: ${response.code()}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    // 네트워크 오류 처리
                    Toast.makeText(
                        holder.itemView.context,
                        "네트워크 오류 발생: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        // 신고 버튼 클릭 리스너 설정
        holder.reportBtn.setOnClickListener {
            // 신고 사유 선택 다이얼로그 표시
            val reasons = arrayOf("ABUSIVE", "DEROGATORY", "ADVERTISEMENT")

            val builder = AlertDialog.Builder(holder.itemView.context)
            builder.setTitle("신고 사유 선택")
            builder.setItems(reasons) { _, which ->
                val selectedReason = reasons[which]

                // 토큰 가져오기
                val accessToken = TokenManager(holder.itemView.context).getAccessToken()

                // 액세스 토큰 확인
                if (accessToken.isNullOrEmpty()) {
                    Toast.makeText(
                        holder.itemView.context,
                        "로그인이 필요합니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setItems
                }

                // 신고 요청 생성
                val reportRequest = ReportRequest(
                    reviewId = review.id,
                    detail = selectedReason,
                    createdAt = LocalDateTime.now().toString()
                )

                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        // API 요청
                        val response = RetrofitInstance.restaurantApi.reportReview(
                            reportRequest,
                            "Bearer $accessToken"
                        )

                        when (response.code()) {
                            200 -> {
                                Toast.makeText(
                                    holder.itemView.context,
                                    "리뷰 신고가 접수되었습니다.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            400 -> {
                                Toast.makeText(
                                    holder.itemView.context,
                                    "잘못된 요청입니다.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            401 -> {
                                Toast.makeText(
                                    holder.itemView.context,
                                    "인증 오류: 다시 로그인하세요.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            404 -> {
                                Toast.makeText(
                                    holder.itemView.context,
                                    "리뷰를 찾을 수 없습니다.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            500 -> {
                                Toast.makeText(
                                    holder.itemView.context,
                                    "서버 오류가 발생했습니다.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            else -> {
                                Toast.makeText(
                                    holder.itemView.context,
                                    "알 수 없는 오류 발생: ${response.code()}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } catch (e: Exception) {
                        Toast.makeText(
                            holder.itemView.context,
                            "네트워크 오류 발생: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            builder.show()
        }
        // 삭제 버튼 클릭 이벤트 처리
        holder.deleteBtn.setOnClickListener {
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("리뷰 삭제")
                .setMessage("이 리뷰를 삭제하시겠습니까?")
                .setPositiveButton("삭제") { _, _ ->
                    CoroutineScope(Dispatchers.Main).launch {
                        try {
                            Log.d("ReviewDelete", "Deleting reviewId=${review.id}")

                            // API 호출
                            val response = RetrofitInstance.restaurantApi.deleteReview(review.id)

                            when (response.code()) {
                                200 -> {
                                    Toast.makeText(
                                        holder.itemView.context,
                                        "리뷰가 삭제되었습니다.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    // 리뷰 목록에서 삭제 및 UI 업데이트
                                    reviews.toMutableList().removeAt(position)
                                    notifyItemRemoved(position)
                                }
                                404 -> {
                                    Toast.makeText(
                                        holder.itemView.context,
                                        "리뷰를 찾을 수 없습니다.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                else -> {
                                    Toast.makeText(
                                        holder.itemView.context,
                                        "오류 발생: ${response.code()}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        } catch (e: Exception) {
                            Toast.makeText(
                                holder.itemView.context,
                                "네트워크 오류 발생: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                .setNegativeButton("취소", null)
                .show()
        }
    }

    override fun getItemCount(): Int = reviews.size

}

