package SongsMain


import Functions.setInsetsforItems
import SongsMain.Classes.myMediaPlayer
import Utilities.Utils.Companion.dP
import android.content.res.Resources
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.snapping.snapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.setPadding
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.example.composepls.R
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDragHandleView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import de.greenrobot.event.EventBus
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class SongsMain_Base : Fragment(R.layout.fragment_songs_main__base) {

    lateinit var tabsView: TabLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bus: EventBus? = EventBus.getDefault()
        val tabholder: ViewPager2 = requireView().findViewById(R.id.tabHolder)
        tabsView = requireView().findViewById(R.id.tabLayout)

        val main = requireView().findViewById<ConstraintLayout>(R.id.main)



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




        // auto-rolling textview
        requireView().findViewById<TextView>(R.id.serviceColorCode).isSelected = true;


        setInsetsforItems(mutableListOf(main))




        val composeview: ComposeView = requireView().findViewById(R.id.composeview)


        setInsetsforItems(mutableListOf(composeview))


        val bottomsheet: ConstraintLayout = requireView().findViewById(R.id.bottomsheet)

        val behavior = BottomSheetBehavior.from(bottomsheet).apply {

            val value = 50.dP

            this.peekHeight=value
            Log.i("TESTS","PeekHeight of bottom sheet"+value.toString())


            isGestureInsetBottomIgnored=true
            this.state= BottomSheetBehavior.STATE_COLLAPSED
        }


        val scena1 = requireView().findViewById<ConstraintLayout>(R.id.scene1)
        val scena2 = requireView().findViewById<ConstraintLayout>(R.id.scene2)
        scena1.visibility=View.VISIBLE
        scena2.visibility=View.GONE


        behavior.addBottomSheetCallback(object:BottomSheetBehavior.BottomSheetCallback(){
            override fun onStateChanged(bottomSheet: View, newState: Int){
                when(newState){
                    BottomSheetBehavior.STATE_COLLAPSED->{
                        scena1.visibility=View.VISIBLE
                        scena2.visibility=View.GONE
                    }

                    BottomSheetBehavior.STATE_DRAGGING -> {

                    }

                    BottomSheetBehavior.STATE_EXPANDED -> {
                        scena1.visibility=View.GONE
                        scena2.visibility=View.VISIBLE
                    }

                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {

                    }

                    BottomSheetBehavior.STATE_HIDDEN -> {

                    }

                    BottomSheetBehavior.STATE_SETTLING -> {

                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {

                scena1.alpha=Functions.map(slideOffset,0F,1F,1F,0F)
                scena2.alpha= slideOffset

                scena1.visibility=View.VISIBLE
                scena2.visibility=View.VISIBLE
            }
        })







        composeview.setContent {
            //ComposeViewInterrior()
        }





    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Preview(showBackground = true)
    @Composable
    fun ComposeViewInterrior(){

        val sheetState = rememberModalBottomSheetState()
        var isSheetOpen = rememberSaveable {
            mutableStateOf(false)
        }

        val scope = rememberCoroutineScope()


        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = RectangleShape,
            shadowElevation = 2.dp,
            color = Color.Transparent
        )
        {


            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ){

                Button(
                    onClick = {
                        isSheetOpen.value=true

                    }
                ) {
                    Text(text="Open sheet")
                }
            }





            val scaffoldState = rememberBottomSheetScaffoldState()

            BottomSheetScaffold(

                scaffoldState = scaffoldState,
                sheetPeekHeight = TODO(),
                sheetContainerColor = TODO(),
                sheetContentColor = TODO(),
                sheetTonalElevation = TODO(),
                sheetShadowElevation = TODO(),
                sheetDragHandle = TODO(),
                sheetSwipeEnabled = TODO(),
                content = TODO(),
                sheetContent ={ Image(
                    modifier = Modifier.fillMaxWidth(),
                    painter = painterResource(id = R.drawable.comehere),
                    contentDescription = null,
                )
                })


//            if (isSheetOpen.value) {
//                ModalBottomSheet(
//                    sheetState = sheetState,
//                    onDismissRequest = {
//                        isSheetOpen.value = false
//                    }
//                ) {
//
//                }
//            }



        }
    }


    fun restoreSavedState(savedInstanceState: Bundle?){
        if(savedInstanceState!=null) {

            tabsView = requireView().findViewById(R.id.tabLayout)
            tabsView.selectTab(tabsView.getTabAt(savedInstanceState.getInt("tab", 0)))

        }

    }




    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("tab",tabsView.selectedTabPosition)

    }

    override fun onPause() {
        super.onPause()


    }


    override fun onResume() {
        super.onResume()

    }

    override fun onDestroy() {
        super.onDestroy()

    }


}