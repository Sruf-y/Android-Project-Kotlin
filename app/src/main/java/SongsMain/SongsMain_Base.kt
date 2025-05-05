package SongsMain




import DataClasses_Ojects.MediaProgressViewModel
import Functions.setInsetsforItems
import SongsMain.Classes.Events
import SongsMain.Classes.Events.SongWasPaused
import SongsMain.Classes.myMediaPlayer
import SongsMain.Tutorial.Application
import SongsMain.Variables.MusicAppSettings
import Utilities.Utils.Companion.dP
import android.content.pm.ActivityInfo
import android.graphics.Shader
import android.media.Image
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.composepls.R
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.CornerFamily
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import de.greenrobot.event.EventBus
import java.io.File
import java.time.LocalTime


class SongsMain_Base : Fragment(R.layout.fragment_songs_main__base) {




    lateinit var tabsView: TabLayout
    lateinit var tabholder: ViewPager2
    lateinit var main:ConstraintLayout




    val bus: EventBus = EventBus.getDefault()

    var selectedTab: Int=0

    lateinit var appbar: AppBarLayout
    var appbar_verticaloffset:Int=0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        main = requireView().findViewById<ConstraintLayout>(R.id.main)
        tabholder= requireView().findViewById(R.id.tabHolder)
        tabholder.setOffscreenPageLimit(3);
        tabsView = requireView().findViewById(R.id.tabLayout)
        appbar=requireView().findViewById(R.id.appbar)



//        selectedTab = savedInstanceState?.getInt("SELECTED_TAB", 0) ?: 0
//
//        Functions.getSharedPrefferencesStorage(Application.instance).apply {
//            selectedTab=this!!.getInt("Base tab",0)
//
//        }
        applySettings()



        // tabs adapter setup


        setupViewPager()








        val buttonOpenDrawer: ShapeableImageView = requireView().findViewById(R.id.drawerButton)

        buttonOpenDrawer.setOnClickListener {
            bus.post(OpenDrawerEvent())
        }








        // restore the appbar's state
        appbar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            appbar_verticaloffset = verticalOffset
        })

        // search
        val searchButton: ShapeableImageView = requireView().findViewById(R.id.search_button)
        searchButton.setOnClickListener {
            bus.post(Events.SearchButtonPressed())
        }














    }

//     SongWasPaused()
//     SongWasStarted()
//     SongWasReset()
//     SongWasStopped()









    private fun setupViewPager() {
        tabholder.adapter = TabSwipeAdaptor(this)



        TabLayoutMediator(tabsView, tabholder) { tab, position ->
            tab.text = when (position) {
                0 -> "Songs"
                1 -> "Playlists"
                2 -> "Folders"
                3 -> "Artists"
                else -> null
            }
        }.attach()



        // Restore selection after layout is complete


    }


    fun applySettings(){
        MusicAppSettings.applySettings(mutableListOf(main))

    }




    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("SELECTED_TAB", tabsView.selectedTabPosition)

    }


    override fun onPause() {
        super.onPause()

        selectedTab=tabsView.selectedTabPosition

        Functions.getSharedPrefferencesEditor(requireContext()).apply{
            this!!.putInt("Base tab",selectedTab)
            this.putInt("SongsAppBar",appbar_verticaloffset)
            apply()
        }







    }


    override fun onDestroy() {


        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()

        // reselecting the tab
        Functions.getSharedPrefferencesStorage(Application.instance).apply {
            selectedTab=this!!.getInt("Base tab",0)
            appbar_verticaloffset=this.getInt("SongsAppBar",0)

        }
        tabholder.setCurrentItem(selectedTab,false)
        tabsView.selectTab(tabsView.getTabAt(selectedTab))


        //re-setting the appbar's expanded/collapsed state
        appbar.post {
            val params = appbar.layoutParams as CoordinatorLayout.LayoutParams
            val behavior = params.behavior as AppBarLayout.Behavior
            behavior.setTopAndBottomOffset(appbar_verticaloffset) // Collapse fully
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






}