package com.example.uosense

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.uosense.adapters.InfoSuggestionPagerAdapter
import com.example.uosense.databinding.ActivityRestaurantInfoSuggestionBinding
import com.example.uosense.fragments.BasicInfoFragment
import com.example.uosense.fragments.BusinessHoursFragment
import com.example.uosense.fragments.ProductMenuFragment
import com.google.android.material.tabs.TabLayoutMediator

/**
 * **RestaurantInfoSuggestionActivity**
 *
 * 식당 정보 수정 제안을 요청하는 액티비티입니다.
 * 특정 식당의 상품/메뉴, 영업시간, 기본정보 제안 기능을 제공합니다.
 */

class RestaurantInfoSuggestionActivity : AppCompatActivity() {

    /** View Binding 객체 선언 */
    private lateinit var binding: ActivityRestaurantInfoSuggestionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /** 뷰 바인딩 초기화 */
        binding = ActivityRestaurantInfoSuggestionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /** ViewPager와 TabLayout 연결 */
        setupViewPager()

        /** 뒤로 가기 버튼 리스너 설정 */
        binding.backBtn.setOnClickListener { finish() }

        /** 저장 버튼 리스너 설정 */
        binding.saveBtn.setOnClickListener {
            val currentFragment = getCurrentFragment()

            when (currentFragment) {
                is ProductMenuFragment -> {
                    Log.d("ActiveFragment", "현재 프래그먼트: 상품/메뉴")
                    currentFragment.sendProductMenuToServer() /** 상품/메뉴 데이터 전송 */
                }
                is BusinessHoursFragment -> {
                    Log.d("ActiveFragment", "현재 프래그먼트: 영업시간")
                    currentFragment.sendBusinessHoursToServer() /** 영업시간 데이터 전송 */
                }
                is BasicInfoFragment -> {
                    Log.d("ActiveFragment", "현재 프래그먼트: 기본정보")
                    currentFragment.sendBasicInfoToServer() /** 기본정보 데이터 전송 */
                }
                else -> {
                    Log.e("ActiveFragment", "알 수 없는 프래그먼트")
                    Toast.makeText(this, "알 수 없는 프래그먼트입니다.", Toast.LENGTH_SHORT).show() /** 오류 메시지 출력 */
                }
            }
        }
    }

    /** ViewPager와 TabLayout 설정 메서드 */
    private fun setupViewPager() {
        val adapter = InfoSuggestionPagerAdapter(this)
        binding.viewPager.adapter = adapter

        /** TabLayoutMediator를 사용해 Tab과 ViewPager 연결 */
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "상품/메뉴"      /** 첫 번째 탭: 상품/메뉴 */
                1 -> "영업시간"        /** 두 번째 탭: 영업시간 */
                else -> "기본정보"     /** 세 번째 탭: 기본정보 */
            }
        }.attach()
    }

    /** 현재 활성화된 프래그먼트를 가져오는 메서드 */
    private fun getCurrentFragment(): Fragment? {
        val currentPosition = binding.viewPager.currentItem
        return supportFragmentManager.findFragmentByTag("f$currentPosition")
    }
}
