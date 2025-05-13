package SongsMain.Tabs

import SongsMain.Classes.Events
import SongsMain.Classes.Song
import SongsMain.Classes.myExoPlayer
import SongsMain.Settings.MusicAppSettings
import SongsMain.Tutorial.Application
import SongsMain.Tutorial.MusicPlayerService
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.OptIn
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.media3.common.util.UnstableApi
import coil3.Image
import com.bumptech.glide.Glide
import com.example.composepls.R
import com.google.android.material.imageview.ShapeableImageView
import de.greenrobot.event.EventBus
import java.io.File

private const val ARG_PARAM1 = "SONG"


class Fullscreen_Song : Fragment(R.layout.fragment_fullscreen__song) {
    private var song: Song? = null

    lateinit var song_image: ShapeableImageView
    lateinit var song_title: TextView
    lateinit var song_next: ImageView
    lateinit var song_previous: ImageView
    lateinit var song_toggle: CheckBox

    lateinit var main: ConstraintLayout

    val bus = EventBus.getDefault()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            song = it.getParcelable<Song>(ARG_PARAM1)
        }
    }

    @OptIn(UnstableApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bus.register(this)


        main = requireView().findViewById(R.id.main)
        song_image=requireView().findViewById(R.id.fullscreensongimage)
        song_title=requireView().findViewById(R.id.fullscreensongtitle)
        song_next=requireView().findViewById(R.id.fullscreen_next)
        song_previous=requireView().findViewById(R.id.fullscreen_previous)
        song_toggle=requireView().findViewById(R.id.constraintLayout6)

        if(song!=null && myExoPlayer.currentlyPlayingSong!=null){


            song_toggle.isChecked=myExoPlayer.isPlaying

            Glide.with(Application.instance)
                .load(File(myExoPlayer.currentlyPlayingSong!!.thumbnail))
                .error(R.drawable.blank_gray_musical_note)
                .placeholder(R.drawable.blank_gray_musical_note)
                .into(song_image)


            song_title.text= myExoPlayer.currentlyPlayingSong!!.title

            MusicAppSettings.applySettings(mutableListOf(main))

            // TODO

            song_next.setOnClickListener {
                myExoPlayer.playNextInPlaylist()
            }
            song_previous.setOnClickListener {
                myExoPlayer.playPreviousInPlaylist()
            }
            song_toggle.setOnClickListener { myExoPlayer.toggle() }

        }





    }

    @OptIn(UnstableApi::class)
    fun onEvent(event: Events.SongWasStarted){
        Glide.with(Application.instance)
            .load(File(myExoPlayer.currentlyPlayingSong!!.thumbnail))
            .error(R.drawable.blank_gray_musical_note)
            .placeholder(R.drawable.blank_gray_musical_note)
            .into(song_image)


        song_title.text= myExoPlayer.currentlyPlayingSong!!.title

        song_toggle.isChecked=myExoPlayer.isPlaying

    }

    @OptIn(UnstableApi::class)
    fun onEvent(event: Events.SongWasPaused){
        song_toggle.isChecked=myExoPlayer.isPlaying
    }


    override fun onDestroy() {

        bus.unregister(this)

        super.onDestroy()
    }






    companion object {

        @JvmStatic
        fun newInstance(song: Song) =
            Fullscreen_Song().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM1, song)
                }
            }
    }
}