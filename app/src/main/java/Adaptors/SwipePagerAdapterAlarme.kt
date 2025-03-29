package Adaptors


import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.composepls.alarme
import com.example.composepls.clock_calc
import com.example.composepls.timer

class swipePagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {


    override fun getItemCount(): Int {
        return 3;
    }


    override fun createFragment(position: Int): Fragment {



        return when (position) {
            0 -> alarme() // MusicFragment();
            1 -> clock_calc();
            2 -> timer() //LyricsFragment();
            else -> alarme();
        }


    }


}