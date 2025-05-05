package SongsMain

import SongsMain.Classes.Events
import SongsMain.Classes.Song
import SongsMain.Classes.SongListAdapter
import SongsMain.Variables.SongsGlobalVars
import SongsMain.Classes.myMediaPlayer
import SongsMain.Variables.MusicAppSettings
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.TextView
import androidx.activity.SystemBarStyle
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.AtomicReference
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.composepls.R
import de.greenrobot.event.EventBus

class search : AppCompatActivity() {

    val designatedList: ArrayList<Song>
        get() {
            return SongsGlobalVars.publicSongs.songsList!!
        }

    val bus = EventBus.getDefault()

    lateinit var main: ConstraintLayout

    lateinit var recyclerLayoutManager: LinearLayoutManager
    lateinit var audioRecycler: RecyclerView
    lateinit var adaptor: SongListAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_search)


        main = this.findViewById(R.id.main)


        val myStatusBarStyle = SystemBarStyle.dark(getColor(R.color.transparent))
        val myNavigationBarStyle = SystemBarStyle.dark(getColor(R.color.black))

        enableEdgeToEdge(myStatusBarStyle, myNavigationBarStyle)


        applySettings()

        Functions.setInsetsforItems(mutableListOf(main))

        audioRecycler = this.findViewById(R.id.audioRecycler)
        adaptor = SongListAdapter(
            ArrayList<Song>(),
            this,
            {song->

                myMediaPlayer.initializeMediaPlayer()

                myMediaPlayer.setSong(song, AtomicReference(SongsGlobalVars.publicSongs))
                myMediaPlayer.start()

            }, { song ->

            })
        audioRecycler.adapter=adaptor
        adaptor.mList=ArrayList<Song>(designatedList)
        recyclerLayoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)

        audioRecycler.setHasFixedSize(true) // If items have consistent size
        audioRecycler.adapter = adaptor
        audioRecycler.layoutManager = recyclerLayoutManager
        adaptor.notifyDataSetChanged()


        val searchview: SearchView = this.findViewById(R.id.searchView)
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




        bus.register(this)

        audioRecycler.post {
            onEvent(Events.SongWasChanged(null, myMediaPlayer.currentlyPlayingSong))
        }


    }



    fun applySettings(){
        main.background= ContextCompat.getDrawable(this,MusicAppSettings.theme)
    }

    fun onEvent(event:Events.SongWasChanged){


        if(event.lastSong!=null && adaptor.mList.contains(event.lastSong)) {
            (recyclerLayoutManager.findViewByPosition(adaptor.mList.indexOf(event.lastSong))
                ?.findViewById<TextView>(R.id.title))?.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.white
                    )
                )
        }





        if(event.currentSong!=null && adaptor.mList.contains(event.currentSong)) {
            (recyclerLayoutManager.findViewByPosition(adaptor.mList.indexOf(event.currentSong))
                ?.findViewById<TextView>(R.id.title))?.setTextColor(
                    ContextCompat.getColor(
                        this,
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