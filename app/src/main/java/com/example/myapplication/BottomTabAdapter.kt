package com.example.myapplication

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class BottomTabAdapter(fragAct: FragmentActivity) : FragmentStateAdapter(fragAct) {
    override fun getItemCount(): Int = 2
    override fun createFragment(position: Int): Fragment {
        return if(position == 0) SetAlarmFragment()
        else ViewDataFragment()
    }

}