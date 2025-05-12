package SongsMain.Tabs

import Functions.Images
import SongsMain.Classes.Events
import SongsMain.Classes.Playlist
import SongsMain.Classes.PlaylistListAdapter
import SongsMain.Classes.Song
import SongsMain.Classes.SongListAdapter
import SongsMain.Classes.myExoPlayer
import SongsMain.Settings.MusicAppSettings
import SongsMain.Tutorial.Application
import SongsMain.Variables.SongsGlobalVars
import android.graphics.drawable.shapes.Shape
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.annotation.OptIn
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.example.composepls.R
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.imageview.ShapeableImageView
import de.greenrobot.event.EventBus
import java.util.concurrent.atomic.AtomicReference

class Playlist_Viewing : Fragment(R.layout.fragment_playlist__viewing) {

    val bus= EventBus.getDefault()

    var playlist: Playlist?=null
    lateinit var audiorecycler: RecyclerView

    lateinit var adaptor: SongListAdapter
    lateinit var layoutManager: LinearLayoutManager

    lateinit var main: CoordinatorLayout

    lateinit var refreshLayout: SwipeRefreshLayout
    lateinit var collapsingToolbarLayout: CollapsingToolbarLayout

    lateinit var playlist_thumbnail_imageview: ImageView


    lateinit var plusButton: ShapeableImageView
    lateinit var optionsButton: ShapeableImageView


    @OptIn(UnstableApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bus.apply {
            if(!this.isRegistered(this@Playlist_Viewing))
                this.register(this@Playlist_Viewing)
        }

        collapsingToolbarLayout=requireView().findViewById(R.id.collapsingToolbarLayout)
        main=requireView().findViewById(R.id.main)
        refreshLayout=requireView().findViewById(R.id.swiperefresh)
        playlist_thumbnail_imageview=requireView().findViewById(R.id.playlist_viewing_image)

        arguments?.let {
            try {
                playlist=it.getParcelable("playlist", Playlist::class.java)
            }catch(ex: Exception){
                ex.printStackTrace()
            }
        }

        if(playlist!=null){


            MusicAppSettings.applySettings(mutableListOf(main))



                Glide.with(Application.instance)
                    .asDrawable()
                    .load(playlist!!.thumbnail?:R.drawable.blank_gray_musical_note)
                    .placeholder(R.drawable.blank_gray_musical_note)
                    .error(R.drawable.blank_gray_musical_note)
                    .into(playlist_thumbnail_imageview)


            collapsingToolbarLayout.title = playlist!!.title

            audiorecycler=view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.audiorecycler)

            adaptor = SongListAdapter(
                ArrayList(),
                requireContext(),
                {song->


                    myExoPlayer.initializePlayer(requireContext())

                    myExoPlayer.setSong(song,AtomicReference(playlist))
                    myExoPlayer.start()

                },
                {song->

                }
            )


            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL,false)

            audiorecycler.setHasFixedSize(true) // If items have consistent size
            audiorecycler.layoutAnimation = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_fade_in)
            audiorecycler.adapter = adaptor
            audiorecycler.layoutManager = layoutManager



            plusButton=requireView().findViewById(R.id.plusButton)
            optionsButton = requireView().findViewById(R.id.optionsButton)


            playlist?.isUserEditable?.let {
                if(!(it)){
                    // playlist generat de aplicatie, nu este userEditable
                    plusButton.visibility=View.GONE
                }
            }

            adaptor.updateData(playlist?.songsList?:ArrayList<Song>())

            audiorecycler.post {
                onEvent(Events.SongWasChanged(null, myExoPlayer.currentlyPlayingSong))
            }

        }else{
            //requireActivity().onBackPressedDispatcher.onBackPressed()
        }


        refreshLayout.setOnRefreshListener {

            adaptor.updateData(playlist?.songsList?: ArrayList<Song>())

            refreshLayout.isRefreshing=false

        }

    }

    fun onEvent(event:Events.PlaylistWasChanged){
        refreshLayout.isRefreshing=true

        adaptor.updateData(playlist?.songsList?: ArrayList<Song>())

        refreshLayout.isRefreshing=false
    }


    fun onEvent(event:Events.SongWasChanged){


        if(event.lastSong!=null && adaptor.mList.contains(event.lastSong)) {
            (layoutManager.findViewByPosition(adaptor.mList.indexOf(event.lastSong))
                ?.findViewById<TextView>(R.id.title))?.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
        }





        if(event.currentSong!=null && adaptor.mList.contains(event.currentSong)) {
            (layoutManager.findViewByPosition(adaptor.mList.indexOf(event.currentSong))
                ?.findViewById<TextView>(R.id.title))?.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.amber
                    )
                )
        }
    }


    override fun onDestroy() {
        super.onDestroy()

        bus.apply {
            if(this.isRegistered(this@Playlist_Viewing))
                this.unregister(this@Playlist_Viewing)
        }
    }

    // TODO just to make this green // create an instance with the given arguments, this works KIND OF like a constructor
    companion object {
        fun newInstance(playlist:Playlist): Playlist_Viewing {
            val fragment = Playlist_Viewing()
            val args = Bundle()
            args.putParcelable("playlist", playlist)
            fragment.arguments = args
            return fragment
        }
    }
}