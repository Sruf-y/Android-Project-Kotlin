package SongsMain




import DataClasses_Ojects.MediaProgressViewModel
import Functions.setInsetsforItems
import SongsMain.Classes.Events
import SongsMain.Classes.Events.SongWasPaused
import SongsMain.Classes.myMediaPlayer
import SongsMain.Tutorial.Application
import SongsMain.Variables.MusicAppSettings
import Utilities.Utils.Companion.dP
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

    private val progressViewModel: MediaProgressViewModel by viewModels()
    private val progressViewModel2: MediaProgressViewModel by viewModels()


    lateinit var tabsView: TabLayout
    lateinit var tabholder: ViewPager2
    lateinit var main:ConstraintLayout

    lateinit var bottomsheetCol_musicToggle: ConstraintLayout
    lateinit var bottomsheetCol_musictitle: TextView
    lateinit var bottomsheetCol_musicImage: ShapeableImageView
    lateinit var bottomsheetCol_musicBackground: ImageView
    lateinit var progressbarCol: ProgressBar

    lateinit var expanded_musicToggle: ConstraintLayout
    lateinit var expanded_musicToggleCheckbox: CheckBox
    lateinit var expanded_SkipForward: ConstraintLayout
    lateinit var expanded_SkipBackward: ConstraintLayout
    lateinit var expanded_Seekbar: SeekBar
    lateinit var expanded_Background: ImageView
    lateinit var expanded_musicTitle: TextView


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





        setInsetsforItems(mutableListOf(main))




        val composeview: ComposeView = requireView().findViewById(R.id.composeview)


        setInsetsforItems(mutableListOf(composeview))


        val bottomsheet: ConstraintLayout = requireView().findViewById(R.id.bottomsheet)

        val behavior = BottomSheetBehavior.from(bottomsheet).apply {

            val value = 60.dP

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





        //expandedinitial settings
        expanded_Seekbar=requireView().findViewById(R.id.expanded_seekbar)
        expanded_Background=requireView().findViewById(R.id.expanded_background)
        expanded_musicTitle=requireView().findViewById(R.id.expanded_title)
        //expanded_SkipForward
        //expanded_SkipBackward
        expanded_musicToggle=requireView().findViewById(R.id.expanded_play_pause)
        expanded_musicToggleCheckbox=requireView().findViewById(R.id.expanded_checkbox)




        //collapsed values initial settings

        bottomsheetCol_musicBackground=requireView().findViewById(R.id.bottomSheetCollapsedBackground)
        bottomsheetCol_musicImage=requireView().findViewById(R.id.bottomSheetCollapsedImage)
        bottomsheetCol_musictitle=requireView().findViewById(R.id.serviceColorCode)
        bottomsheetCol_musicToggle=requireView().findViewById(R.id.bottomSheetCollapsedToggle)


        //setup progressbar
        progressbarCol = requireView().findViewById(R.id.songProgressBar)


        progressViewModel.progress.observe(viewLifecycleOwner) { progress ->
            progressbarCol.progress = progress
        }
        progressViewModel2.progress.observe(viewLifecycleOwner){progress->
            expanded_Seekbar.progress= progress
        }




        // auto-rolling textview
        bottomsheetCol_musictitle.isSelected = true;


        bottomsheetCol_musicToggle.setOnClickListener {

            myMediaPlayer.toggle()
        }
        expanded_musicToggle.setOnClickListener {
            myMediaPlayer.toggle()
        }


        val radius:Float = 15F.dP.toFloat();
        bottomsheetCol_musicImage.setShapeAppearanceModel(bottomsheetCol_musicImage.getShapeAppearanceModel()
            .toBuilder()
            .setAllCorners(CornerFamily.ROUNDED,radius)
            .build());


        // restore the appbar's state
        appbar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            appbar_verticaloffset = verticalOffset
        })

        // search
        val searchButton: ShapeableImageView = requireView().findViewById(R.id.search_button)
        searchButton.setOnClickListener {
            bus.post(Events.SearchButtonPressed())
        }



        composeview.setContent {
            //ComposeViewInterrior()
        }


        onEvent(Events.SongWasChanged(null, myMediaPlayer.currentlyPlayingSong))
        bus.register(this)


        expanded_Seekbar.setOnSeekBarChangeListener(object:SeekBar.OnSeekBarChangeListener{
            var myprogress = 0
            override fun onProgressChanged(
                p0: SeekBar?,
                p1: Int,
                p2: Boolean
            ) {
                myprogress = p0!!.progress
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                progressViewModel2.stopUpdates()
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                if(myMediaPlayer.isPlaying)
                    myMediaPlayer.seekTo(((myprogress.toFloat()/100)* myMediaPlayer.currentlyPlayingSong!!.duration).toLong())
                expanded_Seekbar.progress=myprogress
                progressViewModel2.startUpdates()

            }
        })

        if(myMediaPlayer.currentlyPlayingSong!=null){
            progressViewModel2.startUpdates()
            progressViewModel.startUpdates()
        }


    }

//     SongWasPaused()
//     SongWasStarted()
//     SongWasReset()
//     SongWasStopped()




    fun onEvent(event:SongWasPaused) {

        //for collapsed
        progressViewModel.stopUpdates()
        requireView().findViewById<CheckBox>(R.id.colapsedCheckbox).isChecked=false

        //for expanded
        progressViewModel2.stopUpdates()
        expanded_musicToggleCheckbox.isChecked=false
    }

    fun onEvent(event: Events.SongWasStarted) {

        //for collapsed
        progressViewModel.startUpdates()
        requireView().findViewById<CheckBox>(R.id.colapsedCheckbox).isChecked=true

        //for expanded
        progressViewModel2.startUpdates()
        expanded_musicToggleCheckbox.isChecked=true
    }

    fun onEvent(event:Events.SongWasChanged){
        //for collapsed


        if(event.currentSong!=null) {
            if (File(event.currentSong?.thumbnail).exists()) {
                Glide.with(Application.instance)
                    .load(event.currentSong.thumbnail)
                    .centerCrop()
                    .into(bottomsheetCol_musicImage)

                Glide.with(Application.instance)
                    .load(event.currentSong.thumbnail)
                    .fitCenter()
                    .into(bottomsheetCol_musicBackground)

                Glide.with(Application.instance)
                    .load(event.currentSong.thumbnail)
                    .fitCenter()
                    .into(expanded_Background)
            } else {
                Glide.with(Application.instance)
                    .load(R.drawable.blank_gray_musical_note)
                    .centerCrop()
                    .into(bottomsheetCol_musicImage)

                Glide.with(Application.instance)
                    .load(R.drawable.blank_gray_musical_note)
                    .fitCenter()
                    .into(bottomsheetCol_musicBackground)

                Glide.with(Application.instance)
                    .load(R.drawable.blank)
                    .fitCenter()
                    .into(expanded_Background)
            }

            bottomsheetCol_musictitle.text = event.currentSong.title
            expanded_musicTitle.text = event.currentSong.title

            progressViewModel.startUpdates()
            progressViewModel2.startUpdates()
            requireView().findViewById<CheckBox>(R.id.colapsedCheckbox).isChecked= myMediaPlayer.isPlaying
            expanded_musicToggleCheckbox.isChecked= myMediaPlayer.isPlaying



        }
    }


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
        main.background= ContextCompat.getDrawable(requireContext(),MusicAppSettings.theme)
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






        bottomsheetCol_musictitle.isSelected = false;
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








        bottomsheetCol_musictitle.isSelected = true;
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