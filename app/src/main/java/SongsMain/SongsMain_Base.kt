package SongsMain


import Utilities.Utils.Companion.dP
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.viewpager2.widget.ViewPager2
import com.example.composepls.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import de.greenrobot.event.EventBus
import kotlin.math.max


class SongsMain_Base : Fragment(R.layout.fragment_songs_main__base) {




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bus: EventBus? = EventBus.getDefault()
        val tabholder: ViewPager2 = requireView().findViewById(R.id.tabHolder)
        val tabsView: TabLayout = requireView().findViewById(R.id.tabLayout)
        tabholder.adapter= TabSwipeAdaptor(this)
        TabLayoutMediator(tabsView, tabholder){tab,position->
            when(position){
                0->tab.text="Songs"
                1->tab.text="Playlists"
                2->tab.text="Folders"
                3->tab.text="Artists"
            }
        }.attach()

        val buttonOpenDrawer: ShapeableImageView = requireView().findViewById(R.id.drawerButton)

        buttonOpenDrawer.setOnClickListener {
            bus?.post(OpenDrawerEvent())
        }

        val bottomSheet = requireView().findViewById<ConstraintLayout>(R.id.bottomSheet)
        val behavior = BottomSheetBehavior.from(bottomSheet).apply {
            peekHeight=50.dP
            this.state= BottomSheetBehavior.STATE_COLLAPSED
        }





        // auto-rolling textview
        requireView().findViewById<TextView>(R.id.serviceColorCode).isSelected = true;

        val main = requireView().findViewById<ConstraintLayout>(R.id.main)


        val scene1: ConstraintLayout = requireView().findViewById(R.id.scene1)
        val scene2: ConstraintLayout = requireView().findViewById(R.id.scene2)



        val topSystemBar = activity?.windowManager?.currentWindowMetrics?.windowInsets?.getInsets(WindowInsetsCompat.Type.displayCutout())

        Log.i("TESTS",topSystemBar?.left.toString()+"   "+topSystemBar?.right.toString()+"   "+topSystemBar?.top.toString()+"   "+topSystemBar?.bottom.toString()+" :topSystembars")

        val bottomnavBar = activity?.windowManager?.currentWindowMetrics?.windowInsets?.getInsets(WindowInsetsCompat.Type.navigationBars())

        Log.i("TESTS",bottomnavBar?.left.toString()+"   "+bottomnavBar?.right.toString()+"   "+bottomnavBar?.top.toString()+"   "+bottomnavBar?.bottom.toString())


        if(resources.configuration.orientation== Configuration.ORIENTATION_LANDSCAPE) {
            main.fitsSystemWindows=true

            // NOT WORKING SADLY
            if(bottomnavBar!=null)
                bottomSheet.updatePadding(max(bottomnavBar.left,bottomnavBar.right),0,max(bottomnavBar.left,bottomnavBar.right),0)
        }
        else{
            main.fitsSystemWindows=true

        }

        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback(){
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when(newState){
                    BottomSheetBehavior.STATE_COLLAPSED->{


                        scene2.visibility=View.INVISIBLE

                        scene1.visibility=View.VISIBLE
                        scene1.alpha=1F
                    }
                    BottomSheetBehavior.STATE_EXPANDED->{


                        scene1.visibility=View.INVISIBLE

                        scene2.visibility=View.VISIBLE
                        scene2.alpha=1F


                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {

                scene2.alpha=slideOffset
                scene1.alpha= Functions.map(slideOffset,0F,1F,1F,0F)

                scene1.visibility=View.VISIBLE
                scene2.visibility=View.VISIBLE


                Log.i("TESTS",slideOffset.toString())
            }

        })





















    }




    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)


    }

    override fun onPause() {
        super.onPause()
    }


    override fun onResume() {
        super.onResume()
    }
}