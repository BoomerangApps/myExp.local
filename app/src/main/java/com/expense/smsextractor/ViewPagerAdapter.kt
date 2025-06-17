package com.expense.smsextractor

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.expense.smsextractor.ui.AddedFragment
import com.expense.smsextractor.ui.DraftsFragment

class ViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> DraftsFragment()
            1 -> AddedFragment()
            else -> throw IllegalStateException("Invalid position")
        }
    }
}