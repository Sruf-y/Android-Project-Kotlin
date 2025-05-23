package SongsMain

import DataClasses_Ojects.MediaProgressViewModel
import SongsMain.Classes.Events
import SongsMain.Classes.Events.SongWasPaused
import SongsMain.Classes.MyMediaController
import SongsMain.Classes.myExoPlayer
import SongsMain.Tutorial.Application
import SongsMain.Settings.MusicAppSettings
import SongsMain.Tabs.Fullscreen_Song
import Utilities.Utils.Companion.dP
import android.graphics.Shader
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import androidx.annotation.OptIn
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.bumptech.glide.Glide
import com.example.composepls.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.CornerFamily
import de.greenrobot.event.EventBus
import java.io.File
import kotlin.getValue

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class bottomSheetFragment : Fragment(R.layout.fragment_bottom_sheet), Player.Listener {




    private var param1: String? = null
    private var param2: String? = null


    private val progressViewModel: MediaProgressViewModel by viewModels()
    private val progressViewModel2: MediaProgressViewModel by viewModels()

    val bus= EventBus.getDefault()
    lateinit var main: ConstraintLayout
    lateinit var fragmentContainer: FragmentContainerView

    lateinit var scena1: ConstraintLayout
    lateinit var scena2: ConstraintLayout

    lateinit var bottomsheetCol_musicToggle: ConstraintLayout
    lateinit var bottomsheetCol_musictitle: TextView
    lateinit var bottomsheetCol_musicImage: ShapeableImageView
    lateinit var bottomsheetCol_musicBackground: ImageView
    lateinit var progressbarCol: ProgressBar
    lateinit var bottom_checkbox_musicToggle: CheckBox
    lateinit var bottom_currentpos:TextView
    lateinit var bottom_totalLength:TextView


    lateinit var expanded_musicToggle: ConstraintLayout
    lateinit var expanded_musicToggleCheckbox: CheckBox
    lateinit var expanded_Seekbar: SeekBar
    lateinit var expanded_Background: ImageView
    lateinit var expanded_musicTitle: TextView
    lateinit var expanded_next: ImageView
    lateinit var expanded_prev: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        if(!bus.isRegistered(this))
            bus.register(this)

    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment bottomSheetFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            bottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }


    @OptIn(UnstableApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        main = requireView().findViewById<ConstraintLayout>(R.id.main)
        scena1 = requireView().findViewById<ConstraintLayout>(R.id.scene1)
        scena2 = requireView().findViewById<ConstraintLayout>(R.id.scene2)
        fragmentContainer=requireView().findViewById(R.id.bottomsheet_FragmentContainer)


        scena2.setOnClickListener {
            if(myExoPlayer.currentlyPlayingSong!=null)
                bus.post(Events.MakeCurrentMainFragment(Fullscreen_Song.newInstance(myExoPlayer.currentlyPlayingSong!!)))
        }

        applySettings()

        Functions.setInsetsforItems(mutableListOf(main))


        if(savedInstanceState==null){
            makeCurrentFragment(fragmentContainer, SongsMain_Base(),false,null)
        }

        val bottomsheet: ConstraintLayout = requireView().findViewById(R.id.bottomsheet)

        val behavior = BottomSheetBehavior.from(bottomsheet).apply {

            val value = 60.dP

            this.peekHeight=value
            Log.i("TESTS","PeekHeight of bottom sheet"+value.toString())


            isGestureInsetBottomIgnored=true
            this.state= BottomSheetBehavior.STATE_COLLAPSED
        }


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
        expanded_Background.setRenderEffect(android.graphics.RenderEffect.createBlurEffect(
            20F, 20F,
            Shader.TileMode.REPEAT))
        expanded_musicTitle=requireView().findViewById(R.id.expanded_title)
        //expanded_SkipForward
        //expanded_SkipBackward
        expanded_musicToggle=requireView().findViewById(R.id.expanded_play_pause)
        expanded_musicToggleCheckbox=requireView().findViewById(R.id.expanded_checkbox)
        expanded_prev=requireView().findViewById(R.id.expanded_previous_button)
        expanded_next=requireView().findViewById(R.id.expanded_next_button)
        bottom_checkbox_musicToggle=requireView().findViewById<CheckBox>(R.id.colapsedCheckbox)
        bottom_currentpos=requireView().findViewById(R.id.currentpos)
        bottom_totalLength = requireView().findViewById(R.id.totallength)


        //collapsed values initial settings

        bottomsheetCol_musicBackground=requireView().findViewById(R.id.bottomSheetCollapsedBackground)
        // This applies a blur effect
        bottomsheetCol_musicBackground.setRenderEffect(android.graphics.RenderEffect.createBlurEffect(
            5F, 5f,
            Shader.TileMode.REPEAT))
        bottomsheetCol_musicImage=requireView().findViewById(R.id.bottomSheetCollapsedImage)
        bottomsheetCol_musictitle=requireView().findViewById(R.id.serviceColorCode)
        bottomsheetCol_musicToggle=requireView().findViewById(R.id.bottomSheetCollapsedToggle)


        //setup progressbar
        progressbarCol = requireView().findViewById(R.id.songProgressBar)


        progressViewModel.progress.observe(viewLifecycleOwner) { progress ->
            progressbarCol.progress = progress
        }
        progressViewModel2.progress.observe(viewLifecycleOwner){progress->

            val pos = progress/1000

            val mins:Int = pos/60
            val seconds:Int = pos%60
            if(seconds>10)
                bottom_currentpos.text="${mins}:${seconds}"
            else
                bottom_currentpos.text="${mins}:0${seconds}"
            expanded_Seekbar.progress= progress
        }




        // auto-rolling textview
        bottomsheetCol_musictitle.isSelected = true;


        bottom_checkbox_musicToggle.isChecked= myExoPlayer.exoPlayer?.isPlaying == true

        bottomsheetCol_musicToggle.setOnClickListener {

            myExoPlayer.toggle()
        }
        expanded_musicToggle.setOnClickListener {
            myExoPlayer.toggle()
        }
        expanded_next.setOnClickListener {
            myExoPlayer.playNextInPlaylist()
        }
        expanded_prev.setOnClickListener {
            myExoPlayer.skipToPreviousInPlaylist()
        }


        val radius:Float = 15F.dP.toFloat();
        bottomsheetCol_musicImage.setShapeAppearanceModel(bottomsheetCol_musicImage.getShapeAppearanceModel()
            .toBuilder()
            .setAllCorners(CornerFamily.ROUNDED,radius)
            .build());


        // to update the bottom_sheet on creating it
        onEvent(Events.SongWasChanged(null, myExoPlayer.currentlyPlayingSong))


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
                if(myExoPlayer.currentlyPlayingSong!=null)
                    myExoPlayer.seekTo(myprogress.toLong())
                expanded_Seekbar.progress=myprogress
                progressViewModel2.startUpdates()

                bus.post(Events.SongWas_UsedSeek())

            }
        })

        if(myExoPlayer.currentlyPlayingSong!=null){
            progressViewModel2.startUpdates()
            progressViewModel.startUpdates()
        }





        MyMediaController.addListener(this)




    }

    @OptIn(UnstableApi::class)
    fun onEvent(event:Events.SongWas_UsedSeek){

        expanded_Seekbar.progress= myExoPlayer.getCurrentPosition().toInt()



    }



    fun onEvent(event:Events.MakeCurrent_BottomSheet_Fragment){
        makeCurrentFragment(fragmentContainer,event.fragment,true,event.fragment.arguments)
    }


    fun onEvent(event:Events.ReturnToMainBase){
        //makeCurrentFragment(fragmentContainer, SongsMain.SongsMain_Base())

    }


    private fun makeCurrentFragment( container: FragmentContainerView, fragment:Fragment,addtoBackStack:Boolean=true,arguments:Bundle?,activity: FragmentActivity=requireActivity()) {

        val transaction = activity.supportFragmentManager

        transaction.commit {
            setReorderingAllowed(true)

            if(addtoBackStack)
                addToBackStack(null)

            setCustomAnimations(R.anim.slide_from_right,R.anim.empty_no_animation)

            if(!activity.supportFragmentManager.fragments.contains(fragment)) {
                add(container.id, fragment::class.java, arguments, fragment::class.java.name)

            }
        }
    }

    private fun makeCurrentFragment2(container: FragmentContainerView, fragment: Fragment,addtoBackStack:Boolean=true) {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.setReorderingAllowed(true)
        if(addtoBackStack)
            transaction.addToBackStack(null)

        // Remove any existing fragment in the container
        val currentFragment = requireActivity().supportFragmentManager.findFragmentById(container.id)
        if (currentFragment != null) {
            transaction.remove(currentFragment)
        }

        // Add the new fragment with arguments
        transaction.add(container.id, fragment, fragment::class.java.name)
        transaction.commit()
    }


    @OptIn(UnstableApi::class)
    fun onEvent(event:SongWasPaused) {

        //for collapsed
        progressViewModel.stopUpdates()
        bottom_checkbox_musicToggle.isChecked= myExoPlayer.isPlaying

        //for expanded
        progressViewModel2.stopUpdates()
        expanded_musicToggleCheckbox.isChecked=myExoPlayer.isPlaying
    }

    @OptIn(UnstableApi::class)
    fun onEvent(event: Events.SongWasStarted) {

        val pos = myExoPlayer.currentlyPlayingSong!!.duration/1000
        val mins: Long = pos/60
        val seconds: Long = pos%60

        bottom_totalLength.text="${mins}:${seconds}"

        progressViewModel.startUpdates()
        bottom_checkbox_musicToggle.isChecked=myExoPlayer.isPlaying

        //for expanded
        progressViewModel2.startUpdates()
        expanded_musicToggleCheckbox.isChecked=myExoPlayer.isPlaying
    }

    @OptIn(UnstableApi::class)
    fun onEvent(event:Events.SongWasChanged){
        //for collapsed


        if(event.currentSong!=null) {

            //for collapsed

            expanded_Seekbar.max = event.currentSong.duration.toInt()
            progressbarCol.max=event.currentSong.duration.toInt()

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
            requireView().findViewById<CheckBox>(R.id.colapsedCheckbox).isChecked= myExoPlayer.isPlaying
            expanded_musicToggleCheckbox.isChecked= myExoPlayer.isPlaying



        }
    }







    fun applySettings(){
        MusicAppSettings.applySettings(mutableListOf(main,scena1,scena2))


    }


    override fun onPause() {
        super.onPause()

        bottomsheetCol_musictitle.isSelected = false;
    }

    override fun onDestroy() {
        MyMediaController.removeListener(this)
        bus.unregister(this)
        super.onDestroy()
    }


    fun onEvent(event:Events.SettingsWereChanged){
        applySettings()
    }

    override fun onResume() {
        super.onResume()

        bottomsheetCol_musictitle.isSelected = true;


    }

}