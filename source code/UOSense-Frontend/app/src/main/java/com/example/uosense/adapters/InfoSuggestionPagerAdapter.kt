package com.example.uosense.adapters

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.uosense.fragments.BasicInfoFragment
import com.example.uosense.fragments.BusinessHoursFragment
import com.example.uosense.fragments.ProductMenuFragment

class InfoSuggestionPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ProductMenuFragment()
            1 -> BusinessHoursFragment()
            else -> BasicInfoFragment()
        }
    }
}
