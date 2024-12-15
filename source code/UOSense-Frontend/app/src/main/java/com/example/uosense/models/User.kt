package com.example.uosense.models

data class NewUserRequest(
    val email: String,
    val password: String,
    val nickname: String
)

data class WebmailRequest(
    val email: String,
    val purpose: String // 인증 목적 예: "SIGNUP"
)

data class AuthCodeRequest(
    val email: String,
    val code: String // 사용자가 입력한 인증 코드
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class UserResponse(
    val userId: Int,
    val email: String,
    val nickname: String
)

data class UserProfileResponse(
    val nickname: String,
    val imageUrl: String
)

data class UpdateRequest(
    val image: String? = null
)