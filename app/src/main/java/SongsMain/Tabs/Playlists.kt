package SongsMain.Tabs

import SongsMain.Classes.Events
import SongsMain.Classes.Playlist
import SongsMain.Classes.PlaylistListAdapter
import SongsMain.Variables.SongsGlobalVars
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.composepls.R
import de.greenrobot.event.EventBus


class Playlists : Fragment(R.layout.fragment_playlists) {

    val designatedList = ArrayList<Playlist>().apply {
        add(SongsGlobalVars.RecentlyPlayed)
        add(SongsGlobalVars.MyFavoritesPlaylist)
        add(SongsGlobalVars.hiddenSongs)
        addAll(SongsGlobalVars.playlistsList)
    }
    lateinit var audiorecycler: RecyclerView
    lateinit var adaptor: PlaylistListAdapter
    lateinit var layoutManager: LinearLayoutManager
    val bus= EventBus.getDefault()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bus.register(this)


        audiorecycler=view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.audiorecycler)

        adaptor = PlaylistListAdapter(
            designatedList,
            requireContext(),
            {

            },
            {

            }
        )


        layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL,false)

        audiorecycler.adapter=adaptor
        audiorecycler.layoutManager=layoutManager

        adaptor.notifyDataSetChanged()



        val refreshview: SwipeRefreshLayout = requireView().findViewById(R.id.swiperefresh)

        refreshview.setOnRefreshListener {

            //TODO REFRESH LIST

            refreshview.isRefreshing=false
        }




    }


    fun onEvent(event: Events.SongWasChanged){

    }





    override fun onDestroy() {
        super.onDestroy()
        bus.unregister(this)
    }

}