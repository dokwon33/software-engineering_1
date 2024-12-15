package com.example.uosense

import TokenManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.uosense.databinding.ActivityStartBinding
import com.example.uosense.models.LoginRequest
import com.example.uosense.network.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.util.Base64
import org.json.JSONObject

/**
 * **StartActivity**
 *
 * 로그인을 하는 액티비티입니다.
 * 로그인 후 사용자, 관리자 역할 파악 및 회원가입 액티비티로의 이동 기능을 제공합니다.
 */

class StartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStartBinding
    private lateinit var tokenManager: TokenManager
    private var userRole: String? = null /** admin인지 user인지 저장 */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /** View Binding 초기화 */
        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /** TokenManager 초기화 */
        tokenManager = TokenManager(this)

        /** 로그인 버튼 클릭 이벤트 */
        binding.loginBtn.setOnClickListener {
            val email = binding.emailInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                Toast.makeText(this, "이메일과 비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }

        /** 회원가입 클릭 이벤트 */
        binding.signup.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

        /** 비밀번호 찾기 클릭 이벤트 */
        binding.forgotPassword.setOnClickListener {
            Toast.makeText(this, "아이디/비밀번호 찾기 기능 준비 중", Toast.LENGTH_SHORT).show()
        }
    }

    /** 로그인 처리 */
    private fun loginUser(email: String, password: String) {
        val loginRequest = LoginRequest(email, password)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.restaurantApi.loginUser(loginRequest)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val accessToken =
                            response.headers()["access"]?.removePrefix("Bearer ") ?: ""
                        val refreshToken = response.headers()["Set-Cookie"]?.split(";")
                            ?.find { it.startsWith("refresh=") }
                            ?.substringAfter("refresh=") ?: ""

                        if (accessToken.isNotEmpty() && refreshToken.isNotEmpty()) {
                            /** 토큰 저장 및 role 파싱 */
                            saveTokensToLocal(accessToken, refreshToken)
                            parseRoleFromToken(refreshToken)

                            if (userRole == "USER") {
                                Toast.makeText(
                                    this@StartActivity,
                                    "로그인 성공!" ,
                                    Toast.LENGTH_SHORT
                                ).show()
                                navigateToMainActivity()
                            } else if (userRole == "ADMIN") {
                                Toast.makeText(
                                    this@StartActivity,
                                    "로그인 성공! 관리자 역할: ADMIN",
                                    Toast.LENGTH_SHORT
                                ).show()
                                navigateToControlMainActivity()
                            } else {
                                Toast.makeText(
                                    this@StartActivity,
                                    "알 수 없는 사용자 역할: $userRole",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            Toast.makeText(this@StartActivity, "토큰 발급 실패", Toast.LENGTH_SHORT)
                                .show()
                        }
                    } else {
                        Toast.makeText(
                            this@StartActivity,
                            "로그인 실패",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@StartActivity, "네트워크 오류", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    /** 리프레시 토큰 파싱 처리 */
    private fun parseRoleFromToken(refreshToken: String) {
        try {
            /** JWT 토큰의 Payload 부분만 추출 */
            val payloadBase64 = refreshToken.split(".")[1]
            val payload = String(Base64.decode(payloadBase64, Base64.URL_SAFE))

            /** JSON 객체로 변환하여 role 값 추출 */
            val jsonObject = JSONObject(payload)
            userRole = jsonObject.getString("role") /** admin 또는 user */
        } catch (e: Exception) {
            Log.e("ParseRole", "토큰 파싱 중 오류 발생: ${e.message}")
        }
    }


    private fun saveTokensToLocal(accessToken: String, refreshToken: String) {
        val sharedPreferences = getSharedPreferences("auth_prefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("access_token", accessToken)
        editor.putString("refresh_token", refreshToken)
        editor.apply()
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToControlMainActivity() {
        val intent = Intent(this, MyPageActivity::class.java)
        startActivity(intent)
        finish()
    }
}
