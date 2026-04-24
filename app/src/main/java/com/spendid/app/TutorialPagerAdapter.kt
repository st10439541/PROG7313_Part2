package com.spendid.app

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class TutorialPagerAdapter(activity: TutorialActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> Step1Fragment()
            1 -> Step2Fragment()
            2 -> Step3Fragment()
            3 -> Step4Fragment()
            else -> throw IllegalArgumentException("Invalid position")
        }
    }
}