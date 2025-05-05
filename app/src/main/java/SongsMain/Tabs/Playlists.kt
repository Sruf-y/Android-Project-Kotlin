package SongsMain.Tabs

import SongsMain.BottomSheetDialogs.Add_Playlist
import SongsMain.BottomSheetDialogs.Remove_Playlist
import SongsMain.Classes.Events
import SongsMain.Classes.Playlist
import SongsMain.Classes.PlaylistListAdapter
import SongsMain.Tutorial.Application
import SongsMain.Variables.SongsGlobalVars
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.composepls.R
import de.greenrobot.event.EventBus
import kotlin.collections.indexOf


class Playlists : Fragment(R.layout.fragment_playlists) {

    fun designatedList(): ArrayList<Playlist>{

        (SongsGlobalVars.listOfAllPlaylists.get()).apply {
            if(this.isEmpty())
                return ArrayList<Playlist>()
            else
                return this as ArrayList<Playlist>
        }
    }


    lateinit var nrOfPlaylistsVIEW: TextView
    lateinit var audiorecycler: RecyclerView
    lateinit var adaptor: PlaylistListAdapter
    lateinit var layoutManager: LinearLayoutManager
    val bus= EventBus.getDefault()

    lateinit var plusButton: ImageView
    lateinit var optionsButton: ImageView


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(!bus.isRegistered(this)){
            bus.register(this)
        }
        nrOfPlaylistsVIEW=requireView().findViewById(R.id.nrofPlaylists)
        plusButton=requireView().findViewById(R.id.plusButton)
        optionsButton=requireView().findViewById(R.id.optionsButton)


        audiorecycler=view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.audiorecycler)
        audiorecycler.visibility=View.INVISIBLE

        adaptor = PlaylistListAdapter(
            ArrayList(),
            requireContext(),
            {

                val fragmentInstance = Playlist_Viewing.newInstance(it)

                bus.post(Events.MakeCurrent_BottomSheet_Fragment(fragmentInstance))
            },
            {

            },{
                val view = layoutManager.findViewByPosition(adaptor.mList.indexOf(it))?.findViewById<ImageView>(R.id.songOptions)
                if(!listOf<String>(SongsGlobalVars.RecentlyPlayed.title, SongsGlobalVars.MyFavoritesPlaylist.title,
                        SongsGlobalVars.hiddenSongs.title, SongsGlobalVars.publicSongs.title).contains(it.title)) {
                    val popupMenu = PopupMenu(
                        Application.instance,
                        view,
                        Gravity.START or Gravity.BOTTOM
                    )

                    popupMenu.menuInflater.inflate(R.menu.playlist_individual_menu, popupMenu.menu)

                    popupMenu.setOnMenuItemClickListener { item ->

                        when (item.itemId) {
                            R.id.open->{
                                adaptor.onItemClick.invoke(it)
                            }

                            R.id.rename -> {
                                val add_Playlist_with_title = Add_Playlist.newInstance(it.title)

                                add_Playlist_with_title.show(requireActivity().supportFragmentManager,add_Playlist_with_title.javaClass.name)


                            }

                            R.id.delete -> {
                                val remove_playlist_with_playlistvalue = Remove_Playlist.newInstance(it)

                                remove_playlist_with_playlistvalue.show(requireActivity().supportFragmentManager,remove_playlist_with_playlistvalue.javaClass.name)
                            }
                        }

                        popupMenu.dismiss()
                        true
                        // ask in a dialog if the user is sure they want to delete that playlist
                    }

                popupMenu.show()
                }
                else{
                    // for the constant playlists like public songs, My Favorites etc

                    val popupMenu = PopupMenu(
                        Application.instance,
                        view,
                        Gravity.START or Gravity.BOTTOM
                    )

                    popupMenu.menuInflater.inflate(R.menu.playlist_individual_menu_forconstants, popupMenu.menu)

                    popupMenu.setOnMenuItemClickListener { item ->

                        when (item.itemId) {
                            R.id.open->{
                                adaptor.onItemClick.invoke(it)
                            }


                        }

                        popupMenu.dismiss()
                        true
                        // ask in a dialog if the user is sure they want to delete that playlist
                    }

                    popupMenu.show()
                }
            }
        )


        layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL,false)

        audiorecycler.setHasFixedSize(true) // If items have consistent size
        audiorecycler.layoutAnimation = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_fade_in)
        audiorecycler.adapter = adaptor
        audiorecycler.layoutManager = layoutManager






        onEvent(Events.GlobalDataWasUpdated())





        plusButton.setOnClickListener {
            val addPlaylistButtonSheetDialog= Add_Playlist()

            addPlaylistButtonSheetDialog.show(requireActivity().supportFragmentManager,addPlaylistButtonSheetDialog.javaClass.name)


        }




        nrOfPlaylistsVIEW.text="Playlists: "+adaptor.mList.size





        val refreshview: SwipeRefreshLayout = requireView().findViewById(R.id.swiperefresh)

        refreshview.setOnRefreshListener {





            adaptor.mList.clear()
            adaptor.mList.addAll(designatedList())

            audiorecycler.post {
                adaptor.notifyDataSetChanged()
                refreshview.isRefreshing=false
            }

        }

    }


    fun onEvent(event:Events.GlobalDataWasUpdated){
        adaptor.updateData(designatedList())
        audiorecycler.visibility=View.VISIBLE

        audiorecycler.post {
            nrOfPlaylistsVIEW.text="Playlists: "+adaptor.mList.size
        }
    }


    fun onEvent(event: Events.PlaylistEvents.NotifyAdded){
        var mlistSize = adaptor.mList.size
        adaptor.mList.add(SongsGlobalVars.userMadePlaylists.last())

        adaptor.notifyItemInserted(mlistSize)

        nrOfPlaylistsVIEW.text="Playlists: "+adaptor.mList.size
    }
    fun onEvent(event: Events.PlaylistEvents.NotifyDeleted){

        val playlist = adaptor.mList.find { p->p==event.playlistToDelete }

        val pos = adaptor.mList.indexOf(playlist)

        Log.i("WTF",pos.toString())

        adaptor.mList.removeAt(pos)

        adaptor.notifyItemRangeChanged(pos,adaptor.mList.size+1)

        nrOfPlaylistsVIEW.text="Playlists: "+adaptor.mList.size


    }
    fun onEvent(event: Events.PlaylistEvents.NotifyChanged){
        if(event.playlistThatChanged!=null) {


            val indexof = adaptor.mList.indexOf<Playlist>(event.playlistThatChanged)

            adaptor.mList.clear()
            adaptor.mList.addAll(designatedList())

            Log.i("WTF", indexof.toString())

            adaptor.notifyItemChanged(indexof)
        }
    }




    override fun onDestroy() {
        super.onDestroy()
        if(bus.isRegistered(this))
            bus.unregister(this)
    }

}