package SongsMain

import SongsMain.Classes.Events
import SongsMain.Classes.Song
import SongsMain.Classes.SongListAdapter
import SongsMain.Variables.SongsGlobalVars
import SongsMain.Classes.myMediaPlayer
import SongsMain.Variables.MusicAppSettings
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.TextView
import androidx.activity.addCallback
import androidx.appcompat.widget.SearchView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.composepls.R
import de.greenrobot.event.EventBus

class search : Fragment(R.layout.fragment_search) {

    val designatedList: ArrayList<Song>
        get() {
            return SongsGlobalVars.publicSongs.songsList!!
        }

    val bus = EventBus.getDefault()

    lateinit var main: ConstraintLayout

    lateinit var recyclerLayoutManager: LinearLayoutManager
    lateinit var audioRecycler: RecyclerView
    lateinit var adaptor: SongListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        main = requireView().findViewById(R.id.main)

        applySettings()

        Functions.setInsetsforItems(mutableListOf(main))

        audioRecycler = requireView().findViewById(R.id.audioRecycler)
        adaptor = SongListAdapter(
            ArrayList<Song>(),
            requireContext(),
            {song->

                myMediaPlayer.initializeMediaPlayer()


                if (myMediaPlayer.iPrepared_)
                    myMediaPlayer.reset()
                myMediaPlayer.setSong(song)
                myMediaPlayer.openPlaylist(SongsGlobalVars.publicSongs)
                myMediaPlayer.start()

            }, { song ->

            })
        audioRecycler.adapter=adaptor
        adaptor.mList=ArrayList<Song>(designatedList)
        recyclerLayoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)

        audioRecycler.setHasFixedSize(true) // If items have consistent size
        audioRecycler.adapter = adaptor
        audioRecycler.layoutManager = recyclerLayoutManager
        adaptor.notifyDataSetChanged()


        val searchview: SearchView = requireView().findViewById(R.id.searchView)
        searchview.isIconified = false
        searchview.requestFocus()

        // treci pe un edit_text normal mai incolo si poate chiar creaza un custom searchview bazat pe el
        searchview.setOnQueryTextListener(object:SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(line: String): Boolean {
                Log.i("WTF","designated list size is ${designatedList.size}")
                if(line.isNotEmpty()){
                    adaptor.mList.clear()


                    designatedList.forEach { song->
                        if(song.title.lowercase().contains(line.lowercase().trim())){
                            adaptor.mList.add(song)
                        }
                    }

                    adaptor.notifyDataSetChanged()


                }
                else{
                    adaptor.mList.clear()
                    adaptor.mList.addAll(designatedList)
                    adaptor?.notifyDataSetChanged()
                }

                return true
            }
        })





        val backbutton = requireActivity().onBackPressedDispatcher
        //back button
        backbutton.addCallback(viewLifecycleOwner) {
            // Handle the back press


            bus.post(Events.ReturnToMainBase())
        //isEnabled=false

        }




        bus.register(this)

        audioRecycler.post {
            onEvent(Events.SongWasChanged(null, myMediaPlayer.currentlyPlayingSong))
        }

    }

    fun applySettings(){
        main.background= ContextCompat.getDrawable(requireContext(),MusicAppSettings.theme)
    }

    fun onEvent(event:Events.SongWasChanged){


        if(event.lastSong!=null && adaptor.mList.contains(event.lastSong)) {
            (recyclerLayoutManager.findViewByPosition(adaptor.mList.indexOf(event.lastSong))
                ?.findViewById<TextView>(R.id.title))?.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
        }





        if(event.currentSong!=null && adaptor.mList.contains(event.currentSong)) {
            (recyclerLayoutManager.findViewByPosition(adaptor.mList.indexOf(event.currentSong))
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
        bus.unregister(this)
    }
}