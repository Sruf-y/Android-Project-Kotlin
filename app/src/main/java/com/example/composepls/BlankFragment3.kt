package com.example.composepls

import Adaptors.swipePagerAdapter
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

val timePage:Int =0;
class BlankFragment3 : Fragment(R.layout.fragment_blank3) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pagesView: ViewPager2 = requireView().findViewById(R.id.tabHolder)
        val tabs : TabLayout = requireView().findViewById(R.id.tabLayout)


        pagesView.adapter = swipePagerAdapter(this)

        TabLayoutMediator(tabs,pagesView){tab,position->
            when(position){
                0->tab.text="Alarme"
                1->tab.text="World Time"
                2->tab.text="Timer"
            }
        }.attach()
    }
}