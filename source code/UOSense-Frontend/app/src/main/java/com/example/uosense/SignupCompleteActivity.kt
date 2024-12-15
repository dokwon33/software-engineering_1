package com.example.uosense

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.uosense.databinding.ActivitySignupCompleteBinding

/**
 * **SingupCompleteActivity**
 *
 * 회원가입이 완료되었음을 확인하는 액티비티입니다.
 * 메인 액티비티로의 이동 기능을 제공합니다.
 */

class SignupCompleteActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupCompleteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupCompleteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /** 뒤로가기 버튼 */
        binding.backBtn.setOnClickListener {
            finish()
        }

        /** 물리적 뒤로 가기 활성화 */
        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })

        /** 탐방 시작 버튼 클릭 이벤트 */
        binding.goToMainBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
