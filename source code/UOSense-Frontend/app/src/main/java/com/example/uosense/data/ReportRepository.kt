package com.example.uosense.data

import com.example.uosense.models.Report

object ReportRepository {

    fun getReports(): List<Report> {
        // API 또는 DB 연동 시 실제 데이터로 대체
        return listOf(
            Report("홍길동", null, "★★★★★", "리뷰 제목 1", "리뷰 내용 1", "2024-12-06"),
            Report("김철수", null, "★★★★☆", "리뷰 제목 2", "리뷰 내용 2", "2024-12-05"),
            Report("이영희", null, "★★★☆☆", "리뷰 제목 3", "리뷰 내용 3", "2024-12-04")
        )
    }
}
