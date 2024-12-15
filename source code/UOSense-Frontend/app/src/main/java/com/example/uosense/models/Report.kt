package com.example.uosense.models

data class Report(
    val userName: String,
    val profileImageUri: String?, // 프로필 이미지 URI
    val rating: String,           // 별점
    val title: String,            // 리뷰 제목
    val content: String,          // 리뷰 내용
    val date: String              // 신고 날짜
)
