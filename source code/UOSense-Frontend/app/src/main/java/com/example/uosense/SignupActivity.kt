package com.example.uosense

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.uosense.databinding.ActivitySignupBinding
import android.util.Log
import com.example.uosense.models.AuthCodeRequest
import com.example.uosense.models.NewUserRequest
import com.example.uosense.models.WebmailRequest
import com.example.uosense.network.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * **SignupActivity**
 *
 * 회원가입 액티비티입니다.
 * 회원가입 기능을 제공합니다.
 */

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding

    /** 상태 플래그 */
    private var isEmailVerified = false
    private var isVerificationCodeValid = false
    private var isNicknameVerified = false
    private var isPasswordValid = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /** 뒤로가기 버튼 */
        binding.backBtn.setOnClickListener {
            finish()
        }

        /** 물리적 뒤로 가기 활성화 */
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })

        /** 웹메일 중복 확인 */
        binding.verifyEmailButton.setOnClickListener {
            val email = binding.emailInput.text.toString().trim()

            if (email.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val response = RetrofitInstance.restaurantApi.checkEmail(email)
                        withContext(Dispatchers.Main) {
                            if (response.isSuccessful) {
                                val isEmailValid = response.body() ?: false
                                if (isEmailValid) {
                                    binding.emailErrorMessage.visibility = View.GONE
                                    Toast.makeText(this@SignupActivity, "사용 가능한 이메일입니다.", Toast.LENGTH_SHORT).show()
                                    isEmailVerified = true
                                    updateRegisterButtonState()
                                } else {
                                    binding.emailErrorMessage.visibility = View.VISIBLE
                                    binding.emailErrorMessage.text = "중복된 이메일입니다."
                                    isEmailVerified = false
                                }
                            } else {
                                handleEmailCheckError(response.code(), response.errorBody()?.string())
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@SignupActivity, "네트워크 오류: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                binding.emailErrorMessage.visibility = View.VISIBLE
                binding.emailErrorMessage.text = "유효한 이메일 주소를 입력하세요."
            }
        }

        /** 인증 번호 발송 처리 */
        binding.sendVerificationCodeBtn.setOnClickListener {
            val email = binding.emailInput.text.toString().trim()

            if (email.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val request = WebmailRequest(email, "SIGNUP")
                        val response = RetrofitInstance.restaurantApi.sendAuthCode(request)

                        withContext(Dispatchers.Main) {
                            if (response.isSuccessful && response.body() == true) {
                                Toast.makeText(this@SignupActivity, "인증 코드가 발송되었습니다.", Toast.LENGTH_SHORT).show()
                                binding.verificationLayout.visibility = View.VISIBLE
                                startTimer()
                            } else {
                                handleSendCodeError(response.code(), response.errorBody()?.string())
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@SignupActivity, "네트워크 오류: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                binding.emailErrorMessage.visibility = View.VISIBLE
                binding.emailErrorMessage.text = "유효한 이메일 주소를 입력하세요."
            }
        }

        /** 인증 번호 제출 버튼 클릭 이벤트 */
        binding.submitVerificationCodeBtn.setOnClickListener {
            val email = binding.emailInput.text.toString().trim()
            val code = binding.verificationCodeInput.text.toString().trim()

            if (email.isNotEmpty() && code.isNotEmpty()) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val request = AuthCodeRequest(email, code)
                        val response = RetrofitInstance.restaurantApi.validateCode(request)

                        withContext(Dispatchers.Main) {
                            if (response.isSuccessful && response.body() == true) {
                                Toast.makeText(this@SignupActivity, "인증 번호가 확인되었습니다.", Toast.LENGTH_SHORT).show()
                                binding.timerTextView.visibility = View.GONE
                                if (::countDownTimer.isInitialized) countDownTimer.cancel()
                                isVerificationCodeValid = true
                                updateRegisterButtonState()
                            } else {
                                handleValidateCodeError(response.code(), response.errorBody()?.string())
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@SignupActivity, "네트워크 오류: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "인증 번호를 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }

        /** 닉네임 중복 확인 버튼 클릭 */
        binding.checkNicknameBtn.setOnClickListener {
            val nickname = binding.nicknameInput.text.toString().trim()
            if (nickname.isNotEmpty()) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val response = RetrofitInstance.restaurantApi.checkNickname(nickname)
                        withContext(Dispatchers.Main) {
                            if (response.isSuccessful) {
                                val isNicknameValid = response.body() ?: false
                                if (isNicknameValid) {
                                    binding.nickNameErrorMessage.visibility = View.GONE
                                    Toast.makeText(this@SignupActivity, "사용 가능한 닉네임입니다.", Toast.LENGTH_SHORT).show()
                                    isNicknameVerified = true
                                    updateRegisterButtonState()
                                } else {
                                    binding.nickNameErrorMessage.visibility = View.VISIBLE
                                    binding.nickNameErrorMessage.text = "닉네임이 중복되었습니다."
                                }
                            } else if (response.code() == 400) {
                                binding.nickNameErrorMessage.visibility = View.VISIBLE
                                binding.nickNameErrorMessage.text = "닉네임이 중복되었습니다."
                            } else {
                                Toast.makeText(this@SignupActivity, "서버 오류: ${response.code()}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@SignupActivity, "네트워크 오류: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                binding.nickNameErrorMessage.visibility = View.VISIBLE
                binding.nickNameErrorMessage.text = "닉네임을 입력하세요."
            }
        }

        /** 회원가입 완료 버튼 클릭 이벤트 */
        binding.registerBtn.setOnClickListener {
            val email = binding.emailInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()
            val confirmPassword = binding.passwordConfirmInput.text.toString().trim()
            val nickname = binding.nicknameInput.text.toString().trim()

            /** 비밀번호 유효성 검사 */
            if (!validatePassword(password, confirmPassword)) {
                return@setOnClickListener
            }

            /** 회원가입 처리 */
            registerUser(email, password, nickname)
        }
    }

    /** 회원가입 버튼 활성화 업데이트 */
    private fun updateRegisterButtonState() {
        binding.registerBtn.isEnabled =
            isEmailVerified && isVerificationCodeValid && isNicknameVerified
    }

    /** 비밀번호 검증 */
    private fun validatePassword(password: String, confirmPassword: String): Boolean {
        val passwordPattern = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@\\$!%*?&#])[A-Za-z\\d@\\$!%*?&#]{8,20}$"

        return if (password.isEmpty() || password.length < 8 || password.length > 20) {
            binding.passwordErrorMessage.visibility = View.VISIBLE
            binding.passwordErrorMessage.text = "비밀번호는 최소 8자 이상, 최대 20자 이하여야 합니다."
            isPasswordValid = false
            false
        } else if (!password.matches(Regex(passwordPattern))) {
            binding.passwordErrorMessage.visibility = View.VISIBLE
            binding.passwordErrorMessage.text = "비밀번호는 대,소문자, 숫자, 특수 문자를 포함해야 합니다."
            isPasswordValid = false
            false
        } else if (password != confirmPassword) {
            binding.passwordErrorMessage.visibility = View.VISIBLE
            binding.passwordErrorMessage.text = "비밀번호가 일치하지 않습니다."
            isPasswordValid = false
            false
        } else {
            binding.passwordErrorMessage.visibility = View.GONE
            isPasswordValid = true
            updateRegisterButtonState()
            true
        }
    }



    override fun onDestroy() {
        super.onDestroy()
        if (::countDownTimer.isInitialized) {
            countDownTimer.cancel()
        }
    }

    /** 이메일 중복 버튼 에러 처리 */
    private fun handleEmailCheckError(code: Int, errorBody: String?) {
        when (code) {
            400 -> {
                binding.emailErrorMessage.visibility = View.VISIBLE
                binding.emailErrorMessage.text = "중복된 이메일입니다. 다른 이메일을 사용하세요."
                Log.e("SignupActivity", "400 Error: $errorBody")
            }
            else -> {
                Toast.makeText(this, "알 수 없는 오류가 발생했습니다. ($code)", Toast.LENGTH_SHORT).show()
                Log.e("SignupActivity", "Error: $code, $errorBody")
            }
        }
    }
    /** 인증 번호 발송 에러 처리 */
    private fun handleSendCodeError(code: Int, errorBody: String?) {
        when (code) {
            400 -> {
                Toast.makeText(this, "잘못된 요청입니다. 이메일을 확인하세요.", Toast.LENGTH_SHORT).show()
                Log.e("SignupActivity", "400 Error: $errorBody")
            }
            500 -> {
                Toast.makeText(this, "서버 오류가 발생했습니다. 나중에 다시 시도하세요.", Toast.LENGTH_SHORT).show()
                Log.e("SignupActivity", "500 Error: $errorBody")
            }
            else -> {
                Toast.makeText(this, "알 수 없는 오류가 발생했습니다. ($code)", Toast.LENGTH_SHORT).show()
                Log.e("SignupActivity", "Error: $code, $errorBody")
            }
        }
    }

    /** 인증 번호 제출 에러 처리 */
    private fun handleValidateCodeError(code: Int, errorBody: String?) {
        when (code) {
            400 -> {
                Toast.makeText(this, "인증 번호가 올바르지 않습니다.", Toast.LENGTH_SHORT).show()
                Log.e("SignupActivity", "400 Error: $errorBody")
            }
            500 -> {
                Toast.makeText(this, "서버 오류가 발생했습니다. 나중에 다시 시도하세요.", Toast.LENGTH_SHORT).show()
                Log.e("SignupActivity", "500 Error: $errorBody")
            }
            else -> {
                Toast.makeText(this, "알 수 없는 오류가 발생했습니다. ($code)", Toast.LENGTH_SHORT).show()
                Log.e("SignupActivity", "Error: $code, $errorBody")
            }
        }
    }




    /** 회원가입 처리 */
    private fun registerUser(email: String, password: String, nickname: String) {
        val newUserRequest = NewUserRequest(email, password, nickname)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                /** API 호출 */
                val response = RetrofitInstance.restaurantApi.signupUser(newUserRequest)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        /** 회원가입 성공 시 처리 */
                        val isSuccess = response.body() ?: false
                        if (isSuccess) {
                            /** Access Token과 Refresh Token 추출 */
                            val accessToken = response.headers()["access"]?.removePrefix("Bearer ") ?: ""
                            val refreshToken = response.headers()["Set-Cookie"]?.split(";")
                                ?.find { it.startsWith("refresh=") }
                                ?.substringAfter("refresh=") ?: ""

                            if (accessToken.isNotEmpty() && refreshToken.isNotEmpty()) {
                                /** 토큰 저장 */
                                saveTokensToLocal(accessToken, refreshToken)

                                /** 디버깅용 로그 출력 */
                                Log.d("SignupActivity", "Access Token: $accessToken")
                                Log.d("SignupActivity", "Refresh Token: $refreshToken")

                                Toast.makeText(this@SignupActivity, "회원가입 성공!", Toast.LENGTH_SHORT).show()

                                /** 회원가입 완료 화면으로 이동 */
                                val intent = Intent(this@SignupActivity, SignupCompleteActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                Toast.makeText(this@SignupActivity, "토큰 발급 실패: 서버 응답 확인 필요", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this@SignupActivity, "회원가입 실패. 이미 존재하는 이메일 또는 닉네임입니다.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@SignupActivity, "서버 오류: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SignupActivity, "네트워크 오류: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

/**    사용자 로컬 스토리지에 토큰 저장 */
    private fun saveTokensToLocal(accessToken: String, refreshToken: String) {
        val sharedPreferences = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("access_token", accessToken)
        editor.putString("refresh_token", refreshToken)
        editor.apply()
    }


    /**    사용자 로컬 스토리지에서 토큰 가져오기 */
    private fun getTokensFromLocal(): Pair<String?, String?> {
        val sharedPreferences = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val accessToken = sharedPreferences.getString("access_token", null)
        val refreshToken = sharedPreferences.getString("refresh_token", null)
        return Pair(accessToken, refreshToken)
    }




    /**    웹메일 인증 코드 카운트 처리 */
    private lateinit var countDownTimer: CountDownTimer

    private fun startTimer() {
        if (::countDownTimer.isInitialized) {
            countDownTimer.cancel()
        }

        countDownTimer = object : CountDownTimer(180000, 1000) { /** 3분 (180,000ms) */
            override fun onTick(millisUntilFinished: Long) {
                val minutes = millisUntilFinished / 1000 / 60
                val seconds = millisUntilFinished / 1000 % 60
                binding.timerTextView.text = String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                binding.timerTextView.text = "00:00"
                Toast.makeText(this@SignupActivity, "인증 시간이 초과되었습니다.", Toast.LENGTH_SHORT).show()
            }
        }
        countDownTimer.start()
        binding.timerTextView.visibility = View.VISIBLE
    }

}

