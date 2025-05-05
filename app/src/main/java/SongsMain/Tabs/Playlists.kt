package SongsMain.Tabs

import SongsMain.BottomSheetDialogs.Add_Playlist
import SongsMain.Classes.Events
import SongsMain.Classes.Playlist
import SongsMain.Classes.PlaylistListAdapter
import SongsMain.Classes.Song
import SongsMain.Variables.SongsGlobalVars
import android.media.Image
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.savedstate.serialization.saved
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.composepls.R
import de.greenrobot.event.EventBus


class Playlists : Fragment(R.layout.fragment_playlists) {

    val designatedList: ArrayList<Playlist>
        get() {
            return ArrayList<Playlist>(listOf<Playlist>(SongsGlobalVars.RecentlyPlayed,SongsGlobalVars.MyFavoritesPlaylist,
                SongsGlobalVars.hiddenSongs,
                SongsGlobalVars.publicSongs)).apply { addAll(SongsGlobalVars.playlistsList) }
        }

    lateinit var audiorecycler: RecyclerView
    lateinit var adaptor: PlaylistListAdapter
    lateinit var layoutManager: LinearLayoutManager
    val bus= EventBus.getDefault()

    lateinit var plusButton: ImageView
    lateinit var optionsButton: ImageView


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        plusButton=requireView().findViewById(R.id.plusButton)
        optionsButton=requireView().findViewById(R.id.optionsButton)


        audiorecycler=view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.audiorecycler)

        adaptor = PlaylistListAdapter(
            ArrayList<Playlist>(),
            requireContext(),
            {

                val fragmentInstance = Playlist_Viewing.newInstance(it)

                bus.post(Events.MakeCurrent_BottomSheet_Fragment(fragmentInstance))
            },
            {

            }
        )


        layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL,false)

        audiorecycler.setHasFixedSize(true) // If items have consistent size
        audiorecycler.layoutAnimation = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_fade_in)
        audiorecycler.adapter = adaptor
        audiorecycler.layoutManager = layoutManager

        adaptor.updateData(designatedList)




        plusButton.setOnClickListener {
            val addPlaylistButtonSheetDialog= Add_Playlist()

            addPlaylistButtonSheetDialog.show(requireActivity().supportFragmentManager,addPlaylistButtonSheetDialog.javaClass.name)


        }










        val refreshview: SwipeRefreshLayout = requireView().findViewById(R.id.swiperefresh)

        refreshview.setOnRefreshListener {




            adaptor.updateData(designatedList)

            audiorecycler.post {
                refreshview.isRefreshing=false
            }

        }

    }







    override fun onDestroy() {
        super.onDestroy()
        bus.unregister(this)
    }

}